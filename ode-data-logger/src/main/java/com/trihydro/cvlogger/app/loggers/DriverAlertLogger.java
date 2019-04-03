package com.trihydro.cvlogger.app.loggers;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadata;

import java.sql.SQLException;

import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.service.DriverAlertService;

public class DriverAlertLogger {

	public static void addDriverAlertToOracleDB(OdeData odeData) throws SQLException {
		
		System.out.println("Logging: " + ((OdeLogMetadata)odeData.getMetadata()).getLogFileName());

	    DriverAlertService.insertDriverAlert((OdeLogMetadata) odeData.getMetadata(),
				((OdeDriverAlertPayload) odeData.getPayload()).getAlert());
	}

	public static OdeData processDriverAlertJson(String value) {
		OdeData odeData = null;
		OdeLogMetadata odeDriverAlertMetadata = JsonToJavaConverter.convertDriverAlertMetadataJsonToJava(value);
		OdeDriverAlertPayload odeDriverAlertPayload = JsonToJavaConverter.convertDriverAlertPayloadJsonToJava(value);
		if (odeDriverAlertMetadata != null && odeDriverAlertPayload != null)
			odeData = new OdeData(odeDriverAlertMetadata, odeDriverAlertPayload);
		return odeData;
	}
}