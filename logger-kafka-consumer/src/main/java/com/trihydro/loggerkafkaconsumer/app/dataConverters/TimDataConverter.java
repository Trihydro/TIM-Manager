package com.trihydro.loggerkafkaconsumer.app.dataConverters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.trihydro.library.helpers.JsonToJavaConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeRequestMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.util.JsonUtils;

@Component
public class TimDataConverter {

    public static Gson gson = new Gson();
    private JsonToJavaConverter jsonToJava;

    @Autowired
    public void InjectDependencies(JsonToJavaConverter _jsonToJava) {
        jsonToJava = _jsonToJava;
    }

    public OdeData processTimJson(String value) {

        JsonNode recordGeneratedBy = JsonUtils.getJsonNode(value, "metadata").get("recordGeneratedBy");

        ObjectMapper mapper = new ObjectMapper();

        String recordGeneratedByStr = null;

        try {
            recordGeneratedByStr = mapper.treeToValue(recordGeneratedBy, String.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // if broadcast tim, translate accordingly, else translate as received TIM
        if (recordGeneratedByStr.equals("TMC"))
            return translateBroadcastTimJson(value);
        else
            return translateTimJson(value);
    }

    public OdeData translateTimJson(String value) {
        OdeData odeData = null;
        OdeLogMetadata odeTimMetadata = jsonToJava.convertTimMetadataJsonToJava(value);
        OdeTimPayload odeTimPayload = jsonToJava.convertTimPayloadJsonToJava(value);
        if (odeTimMetadata != null && odeTimPayload != null)
            odeData = new OdeData(odeTimMetadata, odeTimPayload);
        return odeData;
    }

    public OdeData translateBroadcastTimJson(String value) {
        OdeData odeData = null;
        OdeRequestMsgMetadata odeTimMetadata = jsonToJava.convertBroadcastTimMetadataJsonToJava(value);
        OdeTimPayload odeTimPayload = jsonToJava.convertTmcTimTopicJsonToJava(value);
        if (odeTimMetadata != null && odeTimPayload != null)
            odeData = new OdeData(odeTimMetadata, odeTimPayload);
        return odeData;
    }
}