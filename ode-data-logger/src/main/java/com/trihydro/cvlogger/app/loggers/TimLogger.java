package com.trihydro.cvlogger.app.loggers;

import java.sql.Connection;
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
import com.trihydro.library.service.itiscode.ItisCodeService;
import com.trihydro.library.service.tim.ActiveTimItisCodeLogger;
import com.trihydro.library.service.tim.ActiveTimLogger;
import com.trihydro.library.service.tim.DataFrameItisCodeService;
import com.trihydro.library.service.tim.DataFrameService;
import com.trihydro.library.service.tim.NodeXYLogger;
import com.trihydro.library.service.tim.PathLogger;
import com.trihydro.library.service.tim.PathNodeXYLogger;
import com.trihydro.library.service.tim.RegionLogger;
import com.trihydro.library.service.tim.TimService;

public class TimLogger extends BaseLogger{
	
	public TimLogger(Connection connection){
		super(connection);
	}
	
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
    
    public static void addTimToOracleDB(OdeData odeData, Connection connection){
		
		try {
			Long timId = TimService.insertTim((OdeLogMetadataReceived)odeData.getMetadata(), ((OdeTimPayload)odeData.getPayload()).getTim(), connection);
			Long dataFrameId = DataFrameService.insertDataFrame(timId, connection);
			Long pathId = PathLogger.insertPath(connection);
			Long regionId = RegionLogger.insertRegion(dataFrameId, pathId, ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getAnchorPosition(), connection);		
			Long nodeXYId;

			for (J2735TravelerInformationMessage.NodeXY nodeXY : ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getPath().getNodes()) {
				nodeXYId = NodeXYLogger.insertNodeXY(nodeXY, connection);
				PathNodeXYLogger.insertPathNodeXY(nodeXYId, pathId, connection);
			}							
		}
		catch(NullPointerException e){
			System.out.println(e.getMessage());
		}
	}

	public void addActiveTimToOracleDB(OdeData odeData){
		
		// save TIM
		Long timId = TimService.insertTim((OdeLogMetadataReceived)odeData.getMetadata(), ((OdeTimPayload)odeData.getPayload()).getTim(), connection);
		// save DataFrame
		Long dataFrameId = DataFrameService.insertDataFrame(timId, connection);

		String[] timItisCodes = ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getItems();			

		// save active tim
		//Long activeTimId = ActiveTimLogger.insertActiveTim(timId, wydotTim.getFromRm(), wydotTim.getToRm(), wydotTim.getDirection(), timType.getTimTypeId(), startDateTime, endDateTime, wydotTim.getRoute(), clientId, dbUtility.getConnection());
		String name = ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getRegions()[0].getName();
		String[] splitName = name.split("_");
		String direction = splitName[0];
		String route = splitName[1];		

		TimType timType = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>(); 

		// if true, TIM came from WYDOT
		if(splitName.length > 2) {       

			timType = getTimType(splitName[2]);   	
			
			// save DataFrame ITIS codes		
			List<Long> itisCodeIds = setItisCodes(timType, timItisCodes);
		
			for (Long timItisCodeId : itisCodeIds) 
				DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCodeId, connection); 
			
			activeTims = ActiveTimLogger.getActiveTims(Double.parseDouble(splitName[3]), Double.parseDouble(splitName[4]), timType.getTimTypeId(), direction, connection);            		

			Long activeTimId = null;
			// Active TIM exists
			if(activeTims.size() == 0)
				activeTimId = ActiveTimLogger.insertActiveTim(timId, Double.parseDouble(splitName[3]), Double.parseDouble(splitName[4]), direction, timType.getTimTypeId(), ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), null, route, null, connection);		
			else{
				for (ActiveTim activeTim : activeTims) {
					// update Active TIM table TIM Id
					ActiveTimLogger.updateActiveTimTimId(activeTim.getActiveTimId(), timId, connection);

					// if(endDateTime != null)
					// 	ActiveTimLogger.updateActiveTimEndDate(activeTim.getActiveTimId(), endDateTime, dbUtility.getConnection());    
				}      
			}
		}
		else{
			// not from WYDOT application
			// save DataFrame ITIS codes		
			List<Long> itisCodeIds = setItisCodes(null, timItisCodes);
			
			for (Long timItisCodeId : itisCodeIds) 
				DataFrameItisCodeService.insertDataFrameItisCode(dataFrameId, timItisCodeId, connection); 
		}

		// Long activeTimId = ActiveTimLogger.insertActiveTim(timId, 1.0, 1.0, direction, null, ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), null, route, null, connection);
			
		
		// // Add ActiveTim ITIS Codes
		// for (Integer timItisCodeId : itisCodeIds) 
		// 	ActiveTimItisCodeLogger.insertActiveTimItisCode(activeTimId, timItisCodeId, connection); 		 			
	}


	public TimType getTimType(String timTypeName){
     
        TimType timType = timTypes.stream()
        .filter(x -> x.getType().equals(timTypeName))
        .findFirst()
        .orElse(null);

        return timType;
	} 
	
	public List<Long> setItisCodes(String timType, String[] timItisCodes){

		ItisCode itisCode;
		List<Long> itisCodeIds = new ArrayList<Long>();

		if(timType.equals("VSL")) {
			for (String timItisCode : timItisCodes) {
				itisCode = itisCodes.stream()
				.filter(x -> x.getItisCode().equals(Integer.parseInt(timItisCode)))
				.findFirst()
				.orElse(null);
				if(itisCode != null) {
					itisCodeIds.add(new Long(itisCode.getItisCodeId()));     
				}	
			}
		}
		// have null option TODO

		return itisCodeIds;
	}    

	// private Long addActiveTim(Long timId, WydotTimBase wydotTim, List<Integer> itisCodeIds, TimType timType, String startDateTime, String endDateTime, String clientId, Connection connection){       
	
	// 	// Send TIM to Active Tim List        
	// 	Long activeTimId = ActiveTimLogger.insertActiveTim(timId, wydotTim.getFromRm(), wydotTim.getToRm(), wydotTim.getDirection(), timType.getTimTypeId(), startDateTime, endDateTime, wydotTim.getRoute(), clientId, dbUtility.getConnection());

	// 	// Add ActiveTim ITIS Codes
	// 	for(int j = 0; j < itisCodeIds.size(); j++)
	// 		ActiveTimItisCodeLogger.insertActiveTimItisCode(activeTimId, itisCodeIds.get(j), dbUtility.getConnection());  
			
	// 	return activeTimId;
	// }
}