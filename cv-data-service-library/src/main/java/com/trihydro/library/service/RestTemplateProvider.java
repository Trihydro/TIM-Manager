package com.trihydro.library.service;

import com.trihydro.library.helpers.RestTemplateResponseErrorHandler;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateProvider {

    public RestTemplate GetRestTemplate() {
        return new RestTemplate();
    }

    public RestTemplate GetRestTemplate_NoErrors() {
        var rt = new RestTemplate();
        rt.setErrorHandler(new RestTemplateResponseErrorHandler());
        return rt;
    }
}