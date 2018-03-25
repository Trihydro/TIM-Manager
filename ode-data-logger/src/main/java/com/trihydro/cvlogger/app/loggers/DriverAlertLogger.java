package com.trihydro.cvlogger.app.loggers;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.service.DriverAlertService;

public class DriverAlertLogger {
    
	
	public static void addDriverAlertToOracleDB(OdeData odeData) {
		Long driverAlertId = DriverAlertService.insertDriverAlert((OdeLogMetadataReceived)odeData.getMetadata(), ((OdeDriverAlertPayload)odeData.getPayload()).getAlert());	
	}	

	public static OdeData processDriverAlertJson(String value){
		OdeData odeData = null;
		OdeLogMetadataReceived odeDriverAlertMetadata = JsonToJavaConverter.convertDriverAlertMetadataJsonToJava(value);
		OdeDriverAlertPayload odeDriverAlertPayload = JsonToJavaConverter.convertDriverAlertPayloadJsonToJava(value);
		if(odeDriverAlertMetadata != null && odeDriverAlertPayload != null)
			odeData = new OdeData(odeDriverAlertMetadata, odeDriverAlertPayload);
		return odeData;
	}
}