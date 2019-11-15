package com.trihydro.cvlogger.app.loggers;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeRequestMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Geometry;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Path;
import us.dot.its.jpo.ode.util.JsonUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameItisCodeService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.NodeXYService;
import com.trihydro.library.service.PathService;
import com.trihydro.library.service.PathNodeXYService;
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.TimService;

import org.apache.commons.lang3.StringUtils;

import com.trihydro.library.service.TimRsuService;

public class TimLogger extends BaseLogger {

	public static OdeData processTimJson(String value) {

		JsonNode recordGeneratedBy = JsonUtils.getJsonNode(value, "metadata").get("recordGeneratedBy");

		ObjectMapper mapper = new ObjectMapper();

		String recordGeneratedByStr = null;

		try {
			recordGeneratedByStr = mapper.treeToValue(recordGeneratedBy, String.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		// if broadcast tim, translate accordingly, else translate as received TIM
		if (recordGeneratedByStr.equals("TMC"))
			return translateBroadcastTimJson(value);
		else
			return translateTimJson(value);
	}

	public static OdeData translateTimJson(String value) {
		OdeData odeData = null;
		OdeLogMetadata odeTimMetadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload odeTimPayload = JsonToJavaConverter.convertTimPayloadJsonToJava(value);
		if (odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
	}

	public static OdeData translateBroadcastTimJson(String value) {
		OdeData odeData = null;
		OdeRequestMsgMetadata odeTimMetadata = JsonToJavaConverter.convertBroadcastTimMetadataJsonToJava(value);
		OdeTimPayload odeTimPayload = JsonToJavaConverter.convertTmcTimTopicJsonToJava(value);
		if (odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
	}

	public static void addTimToOracleDB(OdeData odeData) {

		try {

			System.out.println("Logging: " + ((OdeLogMetadata) odeData.getMetadata()).getLogFileName());

			Long timId = TimService.insertTim(odeData.getMetadata(),
					((OdeLogMetadata) odeData.getMetadata()).getReceivedMessageDetails(),
					((OdeTimPayload) odeData.getPayload()).getTim(),
					((OdeLogMetadata) odeData.getMetadata()).getRecordType(),
					((OdeLogMetadata) odeData.getMetadata()).getLogFileName(),
					((OdeLogMetadata) odeData.getMetadata()).getSecurityResultCode(), null, null);

			// return if TIM is not inserted
			if (timId == null)
				return;
			Long dataFrameId = null;
			Path path = null;
			Geometry geometry = null;
			// OdePosition3D anchor = null;
			OdeTravelerInformationMessage.DataFrame.Region region = null;
			DataFrame[] dFrames = ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes();
			if (dFrames.length > 0) {
				us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region[] regions = dFrames[0]
						.getRegions();
				if (regions.length > 0) {
					region = regions[0];
					path = regions[0].getPath();
					geometry = regions[0].getGeometry();
					// anchor = regions[0].getAnchorPosition();
				}
				dataFrameId = DataFrameService.insertDataFrame(timId, dFrames[0]);
			}

			if (path != null) {
				Long pathId = PathService.insertPath();
				RegionService.insertRegion(dataFrameId, pathId, region);

				Long nodeXYId;
				for (OdeTravelerInformationMessage.NodeXY nodeXY : path.getNodes()) {
					nodeXYId = NodeXYService.insertNodeXY(nodeXY);
					PathNodeXYService.insertPathNodeXY(nodeXYId, pathId);
				}
			} else if (geometry != null) {
				RegionService.insertRegion(dataFrameId, null, region);
				// RegionService.insertGeometryRegion(dataFrameId, geometry, region);
			} else {
				Utility.logWithDate(
						"addTimToOracleDB - Unable to insert region, no path or geometry found (data_frame_id: "
								+ dataFrameId + ")");
			}

			if (dFrames.length > 0) {
				OdeTravelerInformationMessage.DataFrame.Region[] regions = dFrames[0].getRegions();
				if (regions.length > 0) {
					String regionName = regions[0].getName();
					ActiveTim activeTim = setActiveTimByRegionName(regionName);

					// if this is an RSU TIM
					if (activeTim != null && activeTim.getRsuTarget() != null) {
						// save TIM RSU in DB
						WydotRsu rsu = getRsus().stream().filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget()))
								.findFirst().orElse(null);
						if (rsu != null)
							TimRsuService.insertTimRsu(timId, rsu.getRsuId(), rsu.getRsuIndex());
					}
				}

				// save DataFrame ITIS codes
				for (String timItisCodeId : dFrames[0].getItems()) {
					if (StringUtils.isNumeric(timItisCodeId)) {
						String itisCodeId = getItisCodeId(timItisCodeId);
						if (itisCodeId != null)
							DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, itisCodeId);
					} else
						DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCodeId);
				}
			}
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		}
	}

	// only does one TIM at a time ***
	public static void addActiveTimToOracleDB(OdeData odeData) {

		Utility.logWithDate("Called addActiveTimToOracleDB");
		// variables
		ActiveTim activeTim;

		OdeTimPayload payload = (OdeTimPayload) odeData.getPayload();
		if (payload == null)
			return;
		OdeTravelerInformationMessage tim = payload.getTim();
		if (tim == null)
			return;
		DataFrame[] dframes = tim.getDataframes();
		if (dframes == null || dframes.length == 0)
			return;
		OdeTravelerInformationMessage.DataFrame.Region[] regions = dframes[0].getRegions();
		if (regions == null || regions.length == 0)
			return;
		String name = regions[0].getName();
		if (StringUtils.isEmpty(name) || StringUtils.isBlank(name))
			return;

		// get information from the region name, first check splitname length
		activeTim = setActiveTimByRegionName(name);
		if (activeTim == null)
			return;

		String satRecordId = activeTim.getSatRecordId();

		// save TIM
		Long timId = TimService.insertTim((OdeRequestMsgMetadata) odeData.getMetadata(), null, tim, null, null, null,
				satRecordId, name);

		OdeRequestMsgMetadata metaData = (OdeRequestMsgMetadata) odeData.getMetadata();

		// save DataFrame
		Long dataFrameId = null; // DataFrameService.insertDataFrame(timId, dframes[0]);

		Path path = null;
		Geometry geometry = null;
		Region region = regions[0];
		path = region.getPath();
		geometry = region.getGeometry();
		dataFrameId = DataFrameService.insertDataFrame(timId, dframes[0]);

		if (path != null) {
			Long pathId = PathService.insertPath();
			RegionService.insertRegion(dataFrameId, pathId, region);

			Long nodeXYId;
			for (OdeTravelerInformationMessage.NodeXY nodeXY : path.getNodes()) {
				nodeXYId = NodeXYService.insertNodeXY(nodeXY);
				PathNodeXYService.insertPathNodeXY(nodeXYId, pathId);
			}
		} else if (geometry != null) {
			RegionService.insertRegion(dataFrameId, null, region);
			// RegionService.insertGeometryRegion(dataFrameId, geometry, region);
		} else {
			Utility.logWithDate(
					"addActiveTimToOracleDB - Unable to insert region, no path or geometry found (data_frame_id: "
							+ dataFrameId + ")");
		}

		// TODO : Change to loop through RSU array - doing one rsu for now
		RSU firstRsu = null;
		if (metaData.getRequest() != null && metaData.getRequest().getRsus() != null
				&& metaData.getRequest().getRsus().length > 0) {
			firstRsu = metaData.getRequest().getRsus()[0];
			activeTim.setRsuTarget(firstRsu.getRsuTarget());
		}

		if (metaData.getRequest() != null && metaData.getRequest().getSdw() != null)
			activeTim.setSatRecordId(metaData.getRequest().getSdw().getRecordId());

		activeTim.setStartDateTime(dframes[0].getStartDateTime());
		activeTim.setTimId(timId);

		// if this is an RSU TIM
		if (activeTim.getRsuTarget() != null && metaData.getRequest() != null && metaData.getRequest().getRsus() != null
				&& metaData.getRequest().getRsus().length > 0) {
			// save TIM RSU in DB
			WydotRsu rsu = getRsus().stream().filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget())).findFirst()
					.orElse(null);
			TimRsuService.insertTimRsu(timId, rsu.getRsuId(), firstRsu.getRsuIndex());
		}

		// save DataFrame ITIS codes
		String[] items = dframes[0].getItems();
		if (items.length == 0) {
			System.out.println("No itis codes found to associate with data_frame " + dataFrameId);
		}
		for (String timItisCodeId : items) {
			if (StringUtils.isNumeric(timItisCodeId)) {
				String itisCodeId = getItisCodeId(timItisCodeId);
				if (itisCodeId != null)
					DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, itisCodeId);
			} else
				DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCodeId);
		}

		// set end time if duration is not indefinite
		if (dframes[0].getDurationTime() != 32000) {
			ZonedDateTime zdt = ZonedDateTime.parse(dframes[0].getStartDateTime());
			zdt = zdt.plus(dframes[0].getDurationTime(), ChronoUnit.MINUTES);
			activeTim.setEndDateTime(zdt.toString());
		}

		// if true, TIM came from WYDOT
		if (activeTim.getTimType() != null) {

			ActiveTim activeTimDb = null;

			// if RSU TIM
			if (activeTim.getRsuTarget() != null) // look for active RSU tim that matches incoming TIM
				activeTimDb = ActiveTimService.getActiveRsuTim(activeTim.getClientId(), activeTim.getDirection(),
						activeTim.getRsuTarget());
			else // else look for active SAT tim that matches incoming TIM
				activeTimDb = ActiveTimService.getActiveSatTim(activeTim.getSatRecordId(), activeTim.getDirection());

			// if there is no active TIM, insert new one
			if (activeTimDb == null) {
				ActiveTimService.insertActiveTim(activeTim);
			} else { // else update active TIM
				activeTim.setActiveTimId(activeTimDb.getActiveTimId());
				ActiveTimService.updateActiveTim(activeTim);
			}

		} else {
			// not from WYDOT application
			// just log for now
			System.out.println("Inserting new active_tim, no TimType found - not from WYDOT application");
			ActiveTimService.insertActiveTim(activeTim);
		}
	}

	private static ActiveTim setActiveTimByRegionName(String regionName) {

		if (StringUtils.isBlank(regionName) || StringUtils.isEmpty(regionName)) {
			return null;
		}

		ActiveTim activeTim = new ActiveTim();

		String[] splitName = regionName.split("_");

		if (splitName.length == 0)
			return null;

		if (splitName.length > 0)
			activeTim.setDirection(splitName[0]);
		else
			return activeTim;
		if (splitName.length > 1)
			activeTim.setRoute(splitName[1]);
		else
			return activeTim;
		if (splitName.length > 2)
			activeTim.setMilepostStart(Double.parseDouble(splitName[2]));
		else
			return activeTim;
		if (splitName.length > 3)
			activeTim.setMilepostStop(Double.parseDouble(splitName[3]));
		else
			return activeTim;
		if (splitName.length > 4) {
			// if this is an RSU TIM
			String[] hyphen_array = splitName[4].split("-");
			if (hyphen_array.length > 1) {
				if (hyphen_array[0].equals("SAT")) {
					activeTim.setSatRecordId(hyphen_array[1]);
				} else {
					activeTim.setRsuTarget(hyphen_array[1]);
				}
			}
		} else
			return activeTim;
		if (splitName.length > 5) {
			TimType timType = getTimType((splitName[5]));
			activeTim.setTimType(timType.getType());
			activeTim.setTimTypeId(timType.getTimTypeId());
		} else
			return activeTim;

		if (splitName.length > 6)
			activeTim.setClientId(splitName[6]);
		else
			return activeTim;

		if (splitName.length > 7) {
			try {
				Integer pk = Integer.valueOf(splitName[7]);
				activeTim.setPk(pk);
			} catch (NumberFormatException ex) {
				// the pk won't get set here
			}
		} else
			return activeTim;

		return activeTim;
	}

	public static TimType getTimType(String timTypeName) {

		TimType timType = getTimTypes().stream().filter(x -> x.getType().equals(timTypeName)).findFirst().orElse(null);

		return timType;
	}

	public static List<Integer> getItisCodeIds(String[] items) {

		List<Integer> itisCodeIds = new ArrayList<Integer>();

		for (String item : items) {

			ItisCode itisCode = getItisCodes().stream().filter(x -> x.getItisCode().equals(Integer.parseInt(item)))
					.findFirst().orElse(null);
			if (itisCode != null)
				itisCodeIds.add(itisCode.getItisCodeId());
		}

		return itisCodeIds;
	}

	public static String getItisCodeId(String item) {

		String itisCodeId = null;

		ItisCode itisCode = getItisCodes().stream().filter(x -> x.getItisCode().equals(Integer.parseInt(item)))
				.findFirst().orElse(null);
		if (itisCode != null)
			itisCodeId = itisCode.getItisCodeId().toString();

		return itisCodeId;
	}
}