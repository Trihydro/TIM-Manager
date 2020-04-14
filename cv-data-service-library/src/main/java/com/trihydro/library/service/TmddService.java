package com.trihydro.library.service;

import java.util.List;

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

    @Autowired
    public void InjectDependencies(TmddProps _config) {
        this.config = _config;
    }

    public List<FullEventUpdate> getTmddEvents() {
        String url = String.format("%s/tmdd/all", config.getTmddUrl());
        HttpHeaders headers = new HttpHeaders();
        String encodedCredentials = HttpHeaders.encodeBasicAuth(config.getTmddUser(), config.getTmddPassword(), null);
        headers.setBasicAuth(encodedCredentials);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.GET, entity,
                String.class);
        String body = response.getBody();

        return null;
    }
}