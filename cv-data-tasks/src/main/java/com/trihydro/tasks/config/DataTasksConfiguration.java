package com.trihydro.tasks.config;

import com.trihydro.library.model.RsuDataServiceProps;
import com.trihydro.library.model.SdwProps;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
public class DataTasksConfiguration implements SdwProps, RsuDataServiceProps {

    private String cvRestService;
    private String cvRestServiceDev;    // Temporary
    private String cvRestServiceProd;   // Temporary
    private String rsuDataServiceUrl;
    private String wrapperUrl;
    private String sdwRestUrl;
    private String sdwApiKey;
    private String[] alertAddresses;
    private String fromEmail;
    private String mailHost;
    private int mailPort;


    public String getCvRestService() {
        return cvRestService;
    }

    public void setCvRestService(String cvRestService) {
        this.cvRestService = cvRestService;
    }

    public String getCvRestServiceDev() {
        return cvRestServiceDev;
    }

    public void setCvRestServiceDev(String cvRestServiceDev) {
        this.cvRestServiceDev = cvRestServiceDev;
    }

    public String getCvRestServiceProd() {
        return cvRestServiceProd;
    }

    public void setCvRestServiceProd(String cvRestServiceProd) {
        this.cvRestServiceProd = cvRestServiceProd;
    }

    public String getRsuDataServiceUrl() {
        return rsuDataServiceUrl;
    }

    public void setRsuDataServiceUrl(String rsuDataServiceUrl) {
        this.rsuDataServiceUrl = rsuDataServiceUrl;
    }

    public String getWrapperUrl() {
        return wrapperUrl;
    }

    public void setWrapperUrl(String wrapperUrl) {
        this.wrapperUrl = wrapperUrl;
    }

    public String getSdwRestUrl() {
        return sdwRestUrl;
    }

    public void setSdwRestUrl(String sdwRestUrl) {
        this.sdwRestUrl = sdwRestUrl;
    }

    public String getSdwApiKey() {
        return sdwApiKey;
    }

    public void setSdwApiKey(String sdwApiKey) {
        this.sdwApiKey = sdwApiKey;
    }

    public String[] getAlertAddresses() {
        return alertAddresses;
    }

    public void setAlertAddresses(String[] alertAddresses) {
        this.alertAddresses = alertAddresses;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public int getMailPort() {
        return mailPort;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }
}