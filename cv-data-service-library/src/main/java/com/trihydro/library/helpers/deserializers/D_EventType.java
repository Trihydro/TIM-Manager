package com.trihydro.library.helpers.deserializers;

import java.lang.reflect.Type;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.trihydro.library.model.tmdd.EventType;

public class D_EventType implements JsonDeserializer<EventType> {
    // TMDD Representation
    // { "pavement-conditions": "dry pavement" }
    //
    // Desired Representation
    // { "name": "pavement-conditions", "type": "dry pavement" }

    @Override
    public EventType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Failed parsing JSON source: not an " + EventType.class.getSimpleName());
        }

        JsonObject obj = json.getAsJsonObject();
        Set<String> members = obj.keySet();
        if (members.size() != 1) {
            throw new JsonParseException("Failed parsing JSON source: too many members");
        }

        String name = members.iterator().next();
        String type = obj.get(name).getAsString();

        return new EventType(name, type);
    }

}