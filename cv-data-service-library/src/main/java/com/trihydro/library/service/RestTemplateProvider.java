package com.trihydro.library.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateProvider {
    public static RestTemplate GetRestTemplate() {
        return new RestTemplate();
    }
}