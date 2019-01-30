package com.trihydro.cvlogger.app.loggers;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeRequestMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.util.JsonUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
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
import com.trihydro.library.service.RsuService;
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
		if(recordGeneratedByStr.equals("TMC"))
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
		//OdeTravelerInformationMessage odeTim = JsonToJavaConverter.convertBroadcastTimPayloadJsonToJava(value);
		OdeTimPayload odeTimPayload = JsonToJavaConverter.convertTmcTimTopicJsonToJava(value);
		//OdeTimPayload odeTimPayload = new OdeTimPayload(odeTim);
		if (odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
	}



	public static void addTimToOracleDB(OdeData odeData) {

		try {
			Long timId = TimService.insertTim(odeData.getMetadata(), ((OdeLogMetadata) odeData.getMetadata()).getReceivedMessageDetails(),
					((OdeTimPayload) odeData.getPayload()).getTim(), ((OdeLogMetadata) odeData.getMetadata()).getRecordType(), ((OdeLogMetadata) odeData.getMetadata()).getLogFileName(), ((OdeLogMetadata) odeData.getMetadata()).getSecurityResultCode());

			// odeData.getMetadata().get
			// return if TIM is not inserted
			if (timId == null)
				return;

			Long dataFrameId = DataFrameService.insertDataFrame(timId);
			Long pathId = PathService.insertPath();
			Long regionId = RegionService.insertRegion(dataFrameId, pathId,
					((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0]
							.getAnchorPosition());
			String regionName = ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0]
					.getName();

			ActiveTim activeTim = setActiveTimByRegionName(regionName);

			// if this is an RSU TIM
			if (activeTim.getRsuTarget() != null) {
				// save TIM RSU in DB
				WydotRsu rsu = rsus.stream().filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget())).findFirst()
						.orElse(null);
				if (rsu != null)
					TimRsuService.insertTimRsu(timId, rsu.getRsuId(), rsu.getRsuIndex());
			}

			Long nodeXYId;

			for (OdeTravelerInformationMessage.NodeXY nodeXY : ((OdeTimPayload) odeData.getPayload()).getTim()
					.getDataframes()[0].getRegions()[0].getPath().getNodes()) {
				nodeXYId = NodeXYService.insertNodeXY(nodeXY);
				PathNodeXYService.insertPathNodeXY(nodeXYId, pathId);
			}

			// save DataFrame ITIS codes
			for (String timItisCodeId : ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getItems()) {
				if (StringUtils.isNumeric(timItisCodeId)) {
					if (getItisCodeId(timItisCodeId) != null)
						DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, getItisCodeId(timItisCodeId));
				} else
					DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCodeId);
			}
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		}
	}

	// only does one TIM at a time ***
	public static void addActiveTimToOracleDB(OdeData odeData) {

		// variables
		ActiveTim activeTim;

		// save TIM
		Long timId = TimService.insertTim((OdeRequestMsgMetadata) odeData.getMetadata(), null,
				((OdeTimPayload) odeData.getPayload()).getTim(), null, null, null);

		OdeRequestMsgMetadata metaData = (OdeRequestMsgMetadata) odeData.getMetadata();

		// save DataFrame
		Long dataFrameId = DataFrameService.insertDataFrame(timId);

		// get information from the region name, first check splitname length
		activeTim = setActiveTimByRegionName(
				((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getName());

		if (activeTim == null)
			return;

		// TODO : Change to loop through RSU array - doing one rsu for now
		if (metaData.getRequest() != null && metaData.getRequest().getRsus() != null
				&& metaData.getRequest().getRsus().length > 0)
			activeTim.setRsuTarget(metaData.getRequest().getRsus()[0].getRsuTarget());

		if (metaData.getRequest() != null && metaData.getRequest().getSdw() != null)
			activeTim.setSatRecordId(metaData.getRequest().getSdw().getRecordId());

		activeTim.setStartDateTime(
				((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime());
		activeTim.setTimId(timId);

		// if this is an RSU TIM
		if (activeTim.getRsuTarget() != null) {
			// save TIM RSU in DB
			WydotRsu rsu = rsus.stream().filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget())).findFirst()
					.orElse(null);
			TimRsuService.insertTimRsu(timId, rsu.getRsuId(), metaData.getRequest().getRsus()[0].getRsuIndex());
		}

		// save DataFrame ITIS codes
		for (String timItisCodeId : ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getItems())
			if (StringUtils.isNumeric(timItisCodeId))
				DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, getItisCodeId(timItisCodeId));

		// set end time if duration is not indefinite
		if (((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getDurationTime() != 32000) {
			ZonedDateTime zdt = ZonedDateTime
					.parse(((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime());
			zdt = zdt.plus(((OdeTimPayload) odeData.getPayload()).getTim().getDataframes()[0].getDurationTime(),
					ChronoUnit.MINUTES);
			activeTim.setEndDateTime(zdt.toString());
		}

		// if true, TIM came from WYDOT
		if (activeTim.getTimType() != null) {
		
			ActiveTim activeTimDb = null;

			// if RSU TIM
			if(activeTim.getRsuTarget() != null) // look for active RSU tim that matches incoming TIM
				activeTimDb = ActiveTimService.getActiveRsuTim(activeTim.getClientId(), activeTim.getDirection(), activeTim.getRsuTarget());
			else // else look for active SAT tim that matches incoming TIM
				activeTimDb = ActiveTimService.getActiveSatTim(activeTim.getClientId(), activeTim.getDirection());

			// if there is no active TIM, insert new one
			if(activeTimDb == null){
				ActiveTimService.insertActiveTim(activeTim);
			}
			else{ // else update active TIM
				activeTim.setActiveTimId(activeTimDb.getActiveTimId());
				ActiveTimService.updateActiveTim(activeTim);
			}
		
		} else {
			// not from WYDOT application
			// just log for now
			ActiveTimService.insertActiveTim(activeTim);
		}
	}

	private static ActiveTim setActiveTimByRegionName(String regionName) {

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
			if (splitName[4].split("-")[0].equals("SAT")) {
				activeTim.setSatRecordId(splitName[4].split("-")[1]);
			} else {
				activeTim.setRsuTarget(splitName[4].split("-")[1]);
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

		if (splitName.length > 7)
			activeTim.setPk(Integer.valueOf(splitName[7]));
		else
			return activeTim;

		return activeTim;
	}

	public static TimType getTimType(String timTypeName) {

		TimType timType = timTypes.stream().filter(x -> x.getType().equals(timTypeName)).findFirst().orElse(null);

		return timType;
	}

	public static List<Integer> getItisCodeIds(String[] items) {

		List<Integer> itisCodeIds = new ArrayList<Integer>();

		for (String item : items) {

			ItisCode itisCode = itisCodes.stream().filter(x -> x.getItisCode().equals(Integer.parseInt(item)))
					.findFirst().orElse(null);
			if (itisCode != null)
				itisCodeIds.add(itisCode.getItisCodeId());
		}

		return itisCodeIds;
	}

	public static String getItisCodeId(String item) {

		String itisCodeId = null;

		ItisCode itisCode = itisCodes.stream().filter(x -> x.getItisCode().equals(Integer.parseInt(item))).findFirst()
				.orElse(null);
		if (itisCode != null)
			itisCodeId = itisCode.getItisCodeId().toString();

		return itisCodeId;
	}
}