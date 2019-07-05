package com.trihydro.cvlogger.app.loggers;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;

import java.util.List;

import com.trihydro.cvlogger.app.converters.JsonToJavaConverter;
import com.trihydro.library.service.BsmCoreDataService;
import com.trihydro.library.service.BsmPart2SpveService;
import com.trihydro.library.service.BsmPart2SuveService;
import com.trihydro.library.service.BsmPart2VseService;

public class BsmLogger {
    
	public static OdeData processBsmJson(String value){
		OdeData odeData = null;
		OdeBsmMetadata odeBsmMetadata = JsonToJavaConverter.convertBsmMetadataJsonToJava(value);
		OdeBsmPayload odeBsmPayload = JsonToJavaConverter.convertBsmPayloadJsonToJava(value);
			if(odeBsmMetadata != null && odeBsmPayload != null)
	        	odeData = new OdeData(odeBsmMetadata, odeBsmPayload);
		return odeData;
	}

    public static void addBSMToOracleDB(OdeData odeData, String value) {        
        System.out.println("Logging: " + ((OdeBsmMetadata)odeData.getMetadata()).getLogFileName());

        Long bsmCoreDataId = BsmCoreDataService.insertBSMCoreData((OdeBsmMetadata)odeData.getMetadata(), ((OdeBsmPayload)odeData.getPayload()).getBsm());

        List<J2735BsmPart2Content> partII = ((OdeBsmPayload)odeData.getPayload()).getBsm().getPartII();
        if(bsmCoreDataId != null && !bsmCoreDataId.equals(new Long(0)) && partII!=null){
            for(int i = 0; i < partII.size(); i++) {																			
                if(partII.get(i).getId().name() == "VehicleSafetyExtensions") {					
                    J2735VehicleSafetyExtensions vse = JsonToJavaConverter.convertJ2735VehicleSafetyExtensionsJsonToJava(value, i);
                    if(vse != null)
                        BsmPart2VseService.insertBSMPart2VSE(partII.get(i), vse, bsmCoreDataId);
                }
                else if(partII.get(i).getId().name() == "SpecialVehicleExtensions"){
                    J2735SpecialVehicleExtensions spve = JsonToJavaConverter.convertJ2735SpecialVehicleExtensionsJsonToJava(value, i);
                    if(spve != null)
                        BsmPart2SpveService.insertBSMPart2SPVE(partII.get(i), spve, bsmCoreDataId);
                }
                else if(partII.get(i).getId().name() == "SupplementalVehicleExtensions"){
                    J2735SupplementalVehicleExtensions suve = JsonToJavaConverter.convertJ2735SupplementalVehicleExtensionsJsonToJava(value, i);
                    if(suve != null)
                        BsmPart2SuveService.insertBSMPart2SUVE(partII.get(i), suve, bsmCoreDataId);
                }					
            }	
        }							
    }
}