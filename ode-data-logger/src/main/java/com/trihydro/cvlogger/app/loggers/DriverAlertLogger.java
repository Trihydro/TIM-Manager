package com.trihydro.cvlogger.app.loggers;

import java.sql.Connection;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDriverAlertMetadata;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.service.tim.DataFrameLogger;
import com.trihydro.library.service.tim.NodeXYLogger;
import com.trihydro.library.service.tim.PathLogger;
import com.trihydro.library.service.tim.PathNodeXYLogger;
import com.trihydro.library.service.tim.RegionLogger;
import com.trihydro.library.service.driveralert.DriverAlertService;

public class DriverAlertLogger {
    
	
	public static void addDriverAlertToOracleDB(OdeData odeData, Connection connection) {
		Long driverAlertId = DriverAlertService.insertDriverAlert((OdeDriverAlertMetadata)odeData.getMetadata(), ((OdeDriverAlertPayload)odeData.getPayload()).getAlert(), connection);	
	}	

	public static OdeData processDriverAlertJson(String value){
		OdeData odeData = null;
		OdeDriverAlertMetadata odeDriverAlertMetadata = JsonToJavaConverter.convertDriverAlertMetadataJsonToJava(value);
		OdeDriverAlertPayload odeDriverAlertPayload = JsonToJavaConverter.convertDriverAlertPayloadJsonToJava(value);
		if(odeDriverAlertMetadata != null && odeDriverAlertPayload != null)
			odeData = new OdeData(odeDriverAlertMetadata, odeDriverAlertPayload);
		return odeData;
	}
}