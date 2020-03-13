package com.trihydro.tasks.config;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
@ComponentScan({"com.trihydro.tasks.config", "com.trihydro.library.helpers"})
public class DataTasksConfiguration {

    private String wrapperUrl;
    private String odeUrl;
    private String cvRestService;

    public String getWrapperUrl() {
        return wrapperUrl;
    }

    public String getCvRestService() {
        return cvRestService;
    }

    public void setCvRestService(String cvRestService) {
        this.cvRestService = cvRestService;
    }

    public String getOdeUrl() {
        return odeUrl;
    }

    public void setOdeUrl(String odeUrl) {
        this.odeUrl = odeUrl;
    }

    public void setWrapperUrl(String wrapperUrl) {
        this.wrapperUrl = wrapperUrl;
    }

}