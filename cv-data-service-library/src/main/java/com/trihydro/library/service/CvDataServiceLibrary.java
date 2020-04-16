package com.trihydro.library.service;

import com.trihydro.library.model.CVRestServiceProps;

import org.springframework.beans.factory.annotation.Autowired;

public class CvDataServiceLibrary {
    protected CVRestServiceProps config;
    protected RestTemplateProvider restTemplateProvider;

    @Autowired
    public void InjectDependencies(CVRestServiceProps _cvRestServiceProps,
            RestTemplateProvider _restTemplateProvider) {
        this.config = _cvRestServiceProps;
        this.restTemplateProvider = _restTemplateProvider;
    }
}
