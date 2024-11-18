package com.trihydro.library.service;

import com.trihydro.library.model.HttpLoggingModel;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class LoggingService extends CvDataServiceLibrary {

    public Long LogHttpRequest(HttpLoggingModel httpLoggingModel) {
        String url = String.format("%s/http-logging/add-http-logging", config.getCvRestService());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HttpLoggingModel> entity = new HttpEntity<HttpLoggingModel>(httpLoggingModel, headers);
        ResponseEntity<Long> response = restTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
                Long.class);
        return response.getBody();
    }
}