package com.trihydro.cvlogger.app.loggers;

import java.sql.Connection;
import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.service.bsm.BsmCoreDataService;
import com.trihydro.library.service.bsm.BsmPart2SpveService;
import com.trihydro.library.service.bsm.BsmPart2SuveService;
import com.trihydro.library.service.bsm.BsmPart2VseService;

public class BsmLogger {
    
	public static OdeData processBsmJson(String value){
		OdeData odeData = null;
		OdeBsmMetadata odeBsmMetadata = JsonToJavaConverter.convertBsmMetadataJsonToJava(value);
		OdeBsmPayload odeBsmPayload = JsonToJavaConverter.convertBsmPayloadJsonToJava(value);
			if(odeBsmMetadata != null && odeBsmPayload != null)
	        	odeData = new OdeData(odeBsmMetadata, odeBsmPayload);
		return odeData;
	}

    public static void addBSMToOracleDB(OdeData odeData, String value, Connection connection) {		
        
        Long bsmCoreDataId = BsmCoreDataService.insertBSMCoreData((OdeBsmMetadata)odeData.getMetadata(), ((OdeBsmPayload)odeData.getPayload()).getBsm(), connection);

        if(bsmCoreDataId != null && !bsmCoreDataId.equals(new Long(0))){
            for(int i = 0; i < ((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().size(); i++) {																			
                if(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i).getId().name() == "VehicleSafetyExtensions") {					
                    J2735VehicleSafetyExtensions vse = JsonToJavaConverter.convertJ2735VehicleSafetyExtensionsJsonToJava(value, i);
                    if(vse != null)
                        BsmPart2VseService.insertBSMPart2VSE(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i), vse, bsmCoreDataId, connection);
                }
                else if(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i).getId().name() == "SpecialVehicleExtensions"){
                    J2735SpecialVehicleExtensions spve = JsonToJavaConverter.convertJ2735SpecialVehicleExtensionsJsonToJava(value, i);
                    if(spve != null)
                        BsmPart2SpveService.insertBSMPart2SPVE(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i), spve, bsmCoreDataId, connection);
                }
                else if(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i).getId().name() == "SupplementalVehicleExtensions"){
                    J2735SupplementalVehicleExtensions suve = JsonToJavaConverter.convertJ2735SupplementalVehicleExtensionsJsonToJava(value, i);
                    if(suve != null)
                        BsmPart2SuveService.insertBSMPart2SUVE(((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII().get(i), suve, bsmCoreDataId, connection);
                }					
            }	
        }							
    }
}