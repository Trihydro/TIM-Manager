package com.trihydro.cvlogger.app.loggers;

import com.trihydro.library.helpers.JsonToJavaConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;

@Component
public class BsmLogger {

    private JsonToJavaConverter jsonToJava;

    @Autowired
    public void InjectDependencies(JsonToJavaConverter _jsonToJava) {
        jsonToJava = _jsonToJava;
    }

    public OdeData processBsmJson(String value) {
        OdeData odeData = null;
        OdeBsmMetadata odeBsmMetadata = jsonToJava.convertBsmMetadataJsonToJava(value);
        OdeBsmPayload odeBsmPayload = jsonToJava.convertBsmPayloadJsonToJava(value);
        if (odeBsmMetadata != null && odeBsmPayload != null)
            odeData = new OdeData(odeBsmMetadata, odeBsmPayload);
        return odeData;
    }
}