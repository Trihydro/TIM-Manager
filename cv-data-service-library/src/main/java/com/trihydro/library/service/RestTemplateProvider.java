package com.trihydro.library.service;

import org.springframework.web.client.RestTemplate;

public class RestTemplateProvider {
    public static RestTemplate GetRestTemplate() {
        return new RestTemplate();
    }
}