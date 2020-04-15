package com.trihydro.library.service;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.trihydro.library.helpers.GsonFactory;
import com.trihydro.library.model.TmddProps;
import com.trihydro.library.model.tmdd.FullEventUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TmddService {
    private TmddProps config;
    private GsonFactory gsonFactory;

    @Autowired
    public void InjectDependencies(TmddProps _config, GsonFactory _gsonFactory) {
        this.config = _config;
        this.gsonFactory = _gsonFactory;
    }

    public List<FullEventUpdate> getTmddEvents() throws Exception {
        // Prepare request
        String url = String.format("%s/tmdd/all", config.getTmddUrl());
        HttpHeaders headers = new HttpHeaders();
        String encodedCredentials = HttpHeaders.encodeBasicAuth(config.getTmddUser(), config.getTmddPassword(), null);
        headers.setBasicAuth(encodedCredentials);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        // Get response
        ResponseEntity<String> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET, entity,
                String.class);
        String body = response.getBody();

        Gson gson = gsonFactory.getTmddDeserializer();

        // Remove root
        JsonArray abbrBody = null;
        try {
            abbrBody = gson.fromJson(body, JsonObject.class).get("ns2:fEUMsg").getAsJsonObject().get("FEU")
                    .getAsJsonArray();
        } catch (Exception ex) {
            throw new Exception("Response from TMDD doesn't have the expected structure", ex);
        }

        // Deserialize response
        Type type = new TypeToken<List<FullEventUpdate>>() {
        }.getType();
        List<FullEventUpdate> result = gson.fromJson(abbrBody, type);

        return result;
    }
}