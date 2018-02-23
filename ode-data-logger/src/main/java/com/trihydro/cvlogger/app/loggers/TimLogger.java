package com.trihydro.cvlogger.app.loggers;

import java.sql.Connection;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.service.tim.ActiveTimItisCodeLogger;
import com.trihydro.library.service.tim.ActiveTimLogger;
import com.trihydro.library.service.tim.DataFrameLogger;
import com.trihydro.library.service.tim.NodeXYLogger;
import com.trihydro.library.service.tim.PathLogger;
import com.trihydro.library.service.tim.PathNodeXYLogger;
import com.trihydro.library.service.tim.RegionLogger;
import com.trihydro.library.service.tim.TimService;

public class TimLogger {
    
	public static OdeData processTimJson(String value){
		System.out.println(value);
		OdeData odeData = null;
		OdeTimMetadata odeTimMetadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload odeTimPayload = JsonToJavaConverter.convertTimPayloadJsonToJava(value);
		if(odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
	}
	
	public static OdeData processBroadcastTimJson(String value){
		System.out.println(value);
		OdeData odeData = null;
		OdeTimMetadata odeTimMetadata = JsonToJavaConverter.convertTimMetadataJsonToJava(value);
		OdeTimPayload odeTimPayload = JsonToJavaConverter.convertBroadcastTimPayloadJsonToJava(value);
		if(odeTimMetadata != null && odeTimPayload != null)
			odeData = new OdeData(odeTimMetadata, odeTimPayload);
		return odeData;
    }
    
    public static void addTimToOracleDB(OdeData odeData, Connection connection){
		
		try {
			Long timId = TimService.insertTim((OdeTimMetadata)odeData.getMetadata(), ((OdeTimPayload)odeData.getPayload()).getTim(), connection);
			Long dataFrameId = DataFrameLogger.insertDataFrame(timId, connection);
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

	public static void addActiveTimToOracleDB(OdeData odeData, Connection connection){
		
		// save TIM
//		Long timId = TimService.insertTim(odeData.getMetadata(), odeData.getPayload().getTim(), connection);
		
		// save active tim
		//Long activeTimId = ActiveTimLogger.insertActiveTim(timId, wydotTim.getFromRm(), wydotTim.getToRm(), wydotTim.getDirection(), timType.getTimTypeId(), startDateTime, endDateTime, wydotTim.getRoute(), clientId, dbUtility.getConnection());
//		Long activeTimId = ActiveTimLogger.insertActiveTim(timId, 1.0, 1.0, "eastbound", new Long(1), ((OdeTimPayload)odeData.getPayload()).getTim().getDataframes()[0].getStartDateTime(), null, "I 80", "ABC123", connection);

		// // Add ActiveTim ITIS Codes
		// for(int j = 0; j < itisCodeIds.size(); j++)
		// 	ActiveTimItisCodeLogger.insertActiveTimItisCode(activeTimId, itisCodeIds.get(j), dbUtility.getConnection()); 		 			
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