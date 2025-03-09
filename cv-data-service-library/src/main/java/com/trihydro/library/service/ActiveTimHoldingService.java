package com.trihydro.library.service;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.ActiveTimHolding;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ActiveTimHoldingService extends CvDataServiceLibrary {

    public void insertActiveTimHolding(ActiveTimHolding activeTimHolding) {
        String url = String.format("%s/active-tim-holding/add", config.getCvRestService());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ActiveTimHolding> entity =
            new HttpEntity<>(activeTimHolding, headers);
        ResponseEntity<Long> response = restTemplateProvider.GetRestTemplate()
            .exchange(url, HttpMethod.POST, entity, Long.class);
    }

    public List<ActiveTimHolding> getActiveTimHoldingForRsu(String ipv4Address) {
        String url = String.format("%s/active-tim-holding/get-rsu/%s", config.getCvRestService(),
            ipv4Address);
        ResponseEntity<ActiveTimHolding[]> response =
            restTemplateProvider.GetRestTemplate().getForEntity(url, ActiveTimHolding[].class);
        return Arrays.asList(response.getBody());
    }
}