package com.trihydro.library.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trihydro.library.helpers.deserializers.D_EventType;
import com.trihydro.library.model.tmdd.EventType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class EventTypeDeserializerTest {
    @Test
    public void eventType() {
        String event_type = "{\"pavement-conditions\": \"dry pavement\"}";
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(EventType.class, new D_EventType());

        Gson gson = builder.create();

        EventType result = gson.fromJson(event_type, EventType.class);

        Assertions.assertEquals("pavement-conditions", result.getType());
        Assertions.assertEquals("dry pavement", result.getValue());
    }
}