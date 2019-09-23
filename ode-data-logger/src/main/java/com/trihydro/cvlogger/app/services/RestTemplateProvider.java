package com.trihydro.cvlogger.app.services;

import org.springframework.web.client.RestTemplate;

public class RestTemplateProvider {
    public static RestTemplate GetRestTemplate() {
        return new RestTemplate();
    }
}