package com.trihydro.library.service;

import com.trihydro.library.helpers.RestTemplateResponseErrorHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateProvider {
    private RestTemplateBuilder builder;

    @Autowired
    public RestTemplateProvider(RestTemplateBuilder restTemplateBuilder){
        builder = restTemplateBuilder;
    }

    public RestTemplate GetRestTemplate() {
        return new RestTemplate();
    }

    public RestTemplate GetRestTemplate_NoErrors(){
        return builder.errorHandler(new RestTemplateResponseErrorHandler())
        .build();
    }
}