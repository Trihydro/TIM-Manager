package com.trihydro.cvlogger.app.loggers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
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
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimRsuService;

public class TimLogger extends BaseLogger{
		
	public static OdeData processTimJson(String value){
		System.out.println(value);
		OdeData odeData = null;
		OdeLogMetadataReceived odeTimMetadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload odeTimPayload = JsonToJavaConverter.convertTimPayloadJsonToJava(value);
		if(odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
	}
	
	public static OdeData processBroadcastTimJson(String value){
		System.out.println(value);
		OdeData odeData = null;
		OdeLogMetadataReceived odeTimMetadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		J2735TravelerInformationMessage odeTim = JsonToJavaConverter.convertBroadcastTimPayloadJsonToJava(value);
		OdeTimPayload odeTimPayload = new OdeTimPayload(odeTim);
		if(odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
    }
    
    public static void addTimToOracleDB(OdeData odeData){
		
		try {
			Long timId = TimService.insertTim((OdeLogMetadataReceived)odeData.getMetadata(), ((OdeTimPayload)odeData.getPayload()).getTim());
			Long dataFrameId = DataFrameService.insertDataFrame(timId);
			Long pathId = PathService.insertPath();
			Long regionId = RegionService.insertRegion(dataFrameId, pathId, ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getAnchorPosition());		
			Long nodeXYId;

			for (J2735TravelerInformationMessage.NodeXY nodeXY : ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()) {
				nodeXYId = NodeXYService.insertNodeXY(nodeXY);
				PathNodeXYService.insertPathNodeXY(nodeXYId, pathId);
			}							
		}
		catch(NullPointerException e){
			System.out.println(e.getMessage());
		}
	}

	public static void addActiveTimToOracleDB(OdeData odeData){

		// variables
		DateTimeFormatter formatterMilli = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'-06:00'");
		DateTimeFormatter formatterSec = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'-06:00'");
		DateTimeFormatter formatterMin = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'-06:00'");
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>(); 
		ActiveTim activeTim;
	
		// save TIM
		Long timId = TimService.insertTim((OdeLogMetadataReceived)odeData.getMetadata(), ((OdeTimPayload)odeData.getPayload()).getTim());	

		// save DataFrame
		Long dataFrameId = DataFrameService.insertDataFrame(timId);
		
		// get information from the region name, first check splitname length
		activeTim = setActiveTimByRegionName(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getName());

		if(activeTim == null)
			return;

		activeTim.setStartDateTime(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime());
		activeTim.setTimId(timId);

		// if this is an RSU TIM 
		if(activeTim.getRsuTarget() != null){
			// save TIM RSU in DB		
			WydotRsu rsu = rsus.stream()
				.filter(x -> x.getRsuTarget().equals(activeTim.getRsuTarget()))
				.findFirst()
				.orElse(null);
			TimRsuService.insertTimRsu(timId, rsu.getRsuId());
		}
		
		// save DataFrame ITIS codes				
		List<Integer> itisCodeIds = getItisCodeIds(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getItems());
		
		for (Integer timItisCodeId : itisCodeIds) 
			DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, new Long(timItisCodeId)); 

		String endDateTime = null;
		LocalDateTime dateTime = null;
		if(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getDurationTime() != 32000){
			
			if(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime().contains("."))				
				dateTime = LocalDateTime.parse(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), formatterMilli);
			else if(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime().length() > 22)
				dateTime = LocalDateTime.parse(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), formatterSec);
			else 
				dateTime = LocalDateTime.parse(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), formatterMin);

			endDateTime = dateTime.plusMinutes(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getDurationTime()).toString();
			activeTim.setEndDateTime(endDateTime);
		}

		// if true, TIM came from WYDOT
		if(activeTim.getTimType() != null) {       
			// if there is a client ID
			if(activeTim.getClientId() != null){
				// if its an RSU TIM
				if(activeTim.getRsuTarget() != null)
					activeTims = ActiveTimService.getActiveTimsOnRsuByClientId(activeTim.getRsuTarget(), activeTim.getClientId(), activeTim.getTimTypeId(), activeTim.getDirection());
				// if its a SAT TIM
				else
					activeTims = ActiveTimService.getActiveSatTimsByClientIdDirection(activeTim.getClientId(), activeTim.getTimTypeId(), activeTim.getDirection());
			}
			// else find by road segment
			else{
				// if its an RSU TIM
				if(activeTim.getRsuTarget() != null){ 					
					activeTims = ActiveTimService.getActiveTimsOnRsuByRoadSegment(activeTim.getRsuTarget(), activeTim.getTimTypeId(), activeTim.getMilepostStart(), activeTim.getMilepostStop(), activeTim.getDirection());
				}
				else
					activeTims = ActiveTimService.getActiveSatTimsBySegmentDirection(activeTim.getMilepostStart(), activeTim.getMilepostStop(), activeTim.getTimTypeId(), activeTim.getDirection());            		
			}
			// Active TIM exists
			if(activeTims.size() == 0){						
				ActiveTimService.insertActiveTim(activeTim);			
			}
			else{
				for (ActiveTim ac : activeTims) {
					// update Active TIM table TIM Id
					// ActiveTimService.updateActiveTimTimId(ac.getActiveTimId(), timId);
					// if(endDateTime != null)
					// 	ActiveTimService.updateActiveTimEndDate(ac.getActiveTimId(), endDateTime);
					activeTim.setActiveTimId(ac.getActiveTimId());
					ActiveTimService.updateActiveTim(activeTim);

					// TODO - change to duration
					// if(endDateTime != null)
					// 	ActiveTimLogger.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime, dbUtility.getConnection());    
				}      
			}
		}
		else{
			// not from WYDOT application
			// just log for now
			ActiveTimService.insertActiveTim(activeTim);					
		}			 			
	}

	private static ActiveTim setActiveTimByRegionName(String regionName){

		ActiveTim activeTim = new ActiveTim();

		String[] splitName = regionName.split("_");

		if(splitName.length == 0)
			return null;
		
		if(splitName.length > 0)
			activeTim.setDirection(splitName[0]);
		else
			return activeTim;
		if(splitName.length > 1)
			activeTim.setRoute(splitName[1]);
		else
			return activeTim;
		if(splitName.length > 2)
			activeTim.setMilepostStart(Double.parseDouble(splitName[2]));
		else
			return activeTim;
		if(splitName.length > 3)
			activeTim.setMilepostStop(Double.parseDouble(splitName[3]));
		else
			return activeTim;
		if(splitName.length > 4){
			// if this is an RSU TIM 
			if(splitName[4].split("-")[0].equals("SAT")){
				activeTim.setSatRecordId(splitName[4].split("-")[1]);	
			}
			else{
				activeTim.setRsuTarget(splitName[4].split("-")[1]);	
			}
		}	
		else
			return activeTim;
		if(splitName.length > 5){
			TimType timType = getTimType((splitName[5]));
			activeTim.setTimType(timType.getType());
			activeTim.setTimTypeId(timType.getTimTypeId()); 
		}
		else
			return activeTim;

		if(splitName.length > 6)
			activeTim.setClientId(splitName[6]);
		else
			return activeTim;

		if(splitName.length > 7)
			activeTim.setPk(Integer.valueOf(splitName[7]));
		else
			return activeTim;

		return activeTim;
	}


	public static TimType getTimType(String timTypeName){
     
        TimType timType = timTypes.stream()
        .filter(x -> x.getType().equals(timTypeName))
        .findFirst()
        .orElse(null);

        return timType;
	}   

	public static List<Integer> getItisCodeIds(String[] items){

		List<Integer> itisCodeIds = new ArrayList<Integer>();

		for (String item : items) {
                
			ItisCode itisCode = itisCodes.stream()
				.filter(x -> x.getItisCode().equals(Integer.parseInt(item)))
				.findFirst()
				.orElse(null);
			if(itisCode != null)
				itisCodeIds.add(itisCode.getItisCodeId());                 
		} 

		return itisCodeIds;
	}
}