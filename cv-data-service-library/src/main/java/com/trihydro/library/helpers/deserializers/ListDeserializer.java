package com.trihydro.library.helpers.deserializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public abstract class ListDeserializer<T> implements JsonDeserializer<List<T>> {
    // TMDD Representation
    // { "products": "product": { "serialNumber": "1234" } }
    // or
    // { "products": { "product":
    // [{ "serialNumber": "1234" }, { "serialNumber": "5678" }] } }
    //
    //
    // Desired Representation
    // { "products": [ { "serialNumber": "1234" }] }
    // or
    // { "products": [ { "serialNumber": "1234" }, { "serialNumber": "5678" } ] }

    abstract Class<T> getClazz();

    abstract String getProxyName();

    @Override
    public List<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (!json.isJsonNull() && !json.isJsonObject() && !json.isJsonArray()) {
            throw new JsonParseException("Failed parsing JSON source: invalid type");
        }

        List<T> result = null;

        if (json.isJsonObject()) {
            JsonElement proxyValue = json.getAsJsonObject().get(getProxyName());

            // Either a proxy exists between us and the value, or the value is
            // the item itself.
            if (proxyValue != null) {
                result = context.deserialize(proxyValue, typeOfT);
            } else {
                result = new ArrayList<T>();
                result.add(context.deserialize(json, getClazz()));
            }
        }

        if (json.isJsonArray()) {
            JsonArray elements = json.getAsJsonArray();
            result = new ArrayList<T>();

            for (JsonElement e : elements) {
                result.add(context.deserialize(e, getClazz()));
            }
        }

        return result;
    }
}