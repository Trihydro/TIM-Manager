package com.trihydro.library.helpers;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.trihydro.library.helpers.deserializers.D_EventDescription;
import com.trihydro.library.helpers.deserializers.D_EventElementDetail;
import com.trihydro.library.helpers.deserializers.D_EventLocation;
import com.trihydro.library.helpers.deserializers.D_EventType;
import com.trihydro.library.model.tmdd.EventDescription;
import com.trihydro.library.model.tmdd.EventElementDetail;
import com.trihydro.library.model.tmdd.EventLocation;
import com.trihydro.library.model.tmdd.EventType;

import org.springframework.stereotype.Component;

@Component
public class GsonFactory {
    public Gson getTmddDeserializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);

        // Using TypeToken to assign type, since Java doesn't yet provide a way to
        // represent generic types
        Type type = null;

        // Register EventDescription deserializer
        type = new TypeToken<List<EventDescription>>() {
        }.getType();
        builder.registerTypeAdapter(type, new D_EventDescription());

        // Register EventElementDetail deserializer
        type = new TypeToken<List<EventElementDetail>>() {
        }.getType();
        builder.registerTypeAdapter(type, new D_EventElementDetail());

        // Register EventLocation deserializer
        type = new TypeToken<List<EventLocation>>() {
        }.getType();
        builder.registerTypeAdapter(type, new D_EventLocation());

        // Register EventType deserializer
        builder.registerTypeAdapter(EventType.class, new D_EventType());

        return builder.create();
    }
}