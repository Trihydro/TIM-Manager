package com.trihydro.library.service;

import java.sql.Timestamp;

import com.trihydro.library.model.HttpLoggingModel;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class LoggingService extends CvDataServiceLibrary {
    
    public Long LogHttpRequest(String request, Timestamp requestTime, Timestamp responseTime) {
        String url = String.format("%s/http-logging/add-http-logging", CVRestUrl);
        HttpLoggingModel httpLoggingModel = new HttpLoggingModel();
        httpLoggingModel.setRequest(request);
        httpLoggingModel.setRequestTime(requestTime);
        httpLoggingModel.setResponseTime(responseTime);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HttpLoggingModel> entity = new HttpEntity<HttpLoggingModel>(httpLoggingModel, headers);
        ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
                Long.class);
        return response.getBody();
    }
}