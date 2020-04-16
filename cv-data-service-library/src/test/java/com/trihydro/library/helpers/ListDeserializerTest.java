package com.trihydro.library.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.trihydro.library.helpers.deserializers.ProductDeserializer;
import com.trihydro.library.models.Product;
import com.trihydro.library.models.Store;

import org.junit.Test;

public class ListDeserializerTest {
    // This class uses the Store and Product classes as a proof of concept.
    //
    //
    // A standard, JSON representation of a Store would be:
    // { "products": [ { "serialNumber": "1234" }] }
    //
    //
    // However, WYDOT's TMDD feed would serialize the above in the following,
    // non-standard ways:
    // { "products": "product": { "serialNumber": "1234" } }
    // or
    // { "products": { "product":
    // [{ "serialNumber": "1234" }, { "serialNumber": "5678" }] } }
    //
    //
    // This test class is meant to show that the ListSerializer can deserialize
    // the non-standard JSON arrays provided by WYDOT while still maintaining
    // compatability with standard JSON (so we can consume TMDD feeds from other
    // DOTs)

    private Gson getGson() {
        GsonBuilder builder = new GsonBuilder();

        Type t = new TypeToken<List<Product>>() {
        }.getType();
        builder.registerTypeAdapter(t, new ProductDeserializer());

        return builder.create();
    }

    @Test
    public void deserializes_standardJson() {
        // Arrange
        Gson uut = getGson();
        String json = "{ \"products\": [ { \"serialNumber\": \"1234\" }] }";

        // Act
        Store result = uut.fromJson(json, Store.class);

        // Assert
        assertEquals(1, result.getProducts().size());
        assertEquals("1234", result.getProducts().get(0).getSerialNumber());
    }

    @Test
    public void deserializes_withProxy() {
        // Arrange
        Gson uut = getGson();
        String json = "{ \"products\": { \"product\": [{ \"serialNumber\": \"1234\" }, { \"serialNumber\": \"5678\" }] } }";

        // Act
        Store result = uut.fromJson(json, Store.class);

        // Assert
        assertEquals(2, result.getProducts().size());
        assertEquals("1234", result.getProducts().get(0).getSerialNumber());
        assertEquals("5678", result.getProducts().get(1).getSerialNumber());
    }

    @Test
    public void deserializes_withProxy_singleObject() {
        // Arrange
        Gson uut = getGson();
        String json = "{ \"products\": { \"product\": { \"serialNumber\": \"1234\" } } }";

        // Act
        Store result = uut.fromJson(json, Store.class);

        // Assert
        assertEquals(1, result.getProducts().size());
        assertEquals("1234", result.getProducts().get(0).getSerialNumber());
    }

    @Test
    public void null_json() {
        // Arrange
        Gson uut = getGson();
        String json = null;

        // Act
        Store result = uut.fromJson(json, Store.class);

        // Assert
        assertNull(result);
    }

    @Test(expected = JsonParseException.class)
    public void invalid_json_type() {
        // Arrange
        Gson uut = getGson();
        String json = "{ \"products\": \"a string\" }";

        // Act
        uut.fromJson(json, Store.class);
    }
}