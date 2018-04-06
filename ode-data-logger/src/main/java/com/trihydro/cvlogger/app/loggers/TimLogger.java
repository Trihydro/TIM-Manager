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
		TimType timType = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>(); 
		String satRecordId = null;
		String direction = null;
		String rsuTarget = null;
		Double toRm = null;
		Double fromRm = null;
		String route = null;
		String clientId = null;
	
		// save TIM
	    System.out.println(((OdeTimPayload)odeData.getPayload()).getTim().getTimeStamp());
	    // 2018-03-08T21:45:08.453-07:00
	 	

		Long timId = TimService.insertTim((OdeLogMetadataReceived)odeData.getMetadata(), ((OdeTimPayload)odeData.getPayload()).getTim());	

		// save DataFrame
		Long dataFrameId = DataFrameService.insertDataFrame(timId);

		// save active tim
		String name = ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getName();
		String[] splitName = name.split("_");

		// check splitname length
		if(splitName.length > 1){
			direction = splitName[0];
			route = splitName[1];
		}	
		else	
			return;			

		// check to see if region name is correct
		if(splitName.length < 5)
			return;

		fromRm = Double.parseDouble(splitName[2]);
		toRm = Double.parseDouble(splitName[3]);

		// if this is an RSU TIM 
		if(splitName[4].split("-")[0].equals("RSU")){
			// save TIM RSU in DB
			rsuTarget = splitName[4].split("-")[1];		
			WydotRsu rsu = rsus.stream()
				.filter(x -> x.getRsuTarget().equals(splitName[4].split("-")[1]))
				.findFirst()
				.orElse(null);
			TimRsuService.insertTimRsu(timId, rsu.getRsuId());
		}
		else {  // Satellite TIM
			satRecordId = splitName[4].split("-")[1];	
		}
		// save DataFrame ITIS codes				
		List<Integer> itisCodeIds = getItisCodeIds(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getItems());
		
		for (Integer timItisCodeId : itisCodeIds) 
			DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, new Long(timItisCodeId)); 

		String endDateTime = null;
		if(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getDurationTime() != 32000){				
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'-06:00'");
			LocalDateTime dateTime = LocalDateTime.parse(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), formatter);
			endDateTime = dateTime.plusMinutes(((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getDurationTime()).toString();
		}

		// if true, TIM came from WYDOT
		if(splitName.length > 5) {       
		
			timType = getTimType(splitName[5]);   	
			
			if(splitName.length > 6){
				clientId = splitName[6];
				activeTims = ActiveTimService.getActiveTimsByClientId(clientId);			
			}
			else{
				if(splitName[4].split("-")[0].equals("RSU")){
					//activeTims = ActiveTimService.getActiveRsuTims(Double.parseDouble(splitName[2]), Double.parseDouble(splitName[3]), timType.getTimTypeId(), direction);            		
					if(timType.getType().equals("RC"))
						activeTims = ActiveTimService.getActiveRCTimsOnRsu(rsuTarget, fromRm, toRm, direction);
					else if(timType.getType().equals("VSL"))
						activeTims = ActiveTimService.getActiveVSLTimsOnRsu(rsuTarget, fromRm, toRm, direction);
				}
				else
					activeTims = ActiveTimService.getActiveSatTims(fromRm, toRm, timType.getTimTypeId(), direction);            		
			}
			// Active TIM exists
			if(activeTims.size() == 0){			
				if(splitName.length > 6)
					ActiveTimService.insertActiveTim(timId, fromRm, toRm, direction, timType.getTimTypeId(), ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), endDateTime, route, clientId, satRecordId);		
				else
					ActiveTimService.insertActiveTim(timId, fromRm, toRm, direction, timType.getTimTypeId(), ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), endDateTime, route, null, satRecordId);		
			}
			else{
				for (ActiveTim activeTim : activeTims) {
					// update Active TIM table TIM Id
					ActiveTimService.updateActiveTimTimId(activeTim.getActiveTimId(), timId);
					if(endDateTime != null)
						ActiveTimService.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime);

					// TODO - change to duration
					// if(endDateTime != null)
					// 	ActiveTimLogger.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime, dbUtility.getConnection());    
				}      
			}
		}
		else{
			// not from WYDOT application
			// just log for now
			ActiveTimService.insertActiveTim(timId, fromRm, toRm, direction, null, ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), endDateTime, route, null, satRecordId);					
		}			 			
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