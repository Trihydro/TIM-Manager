package com.trihydro.timrefresh.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

@Component
@ConfigurationProperties("config")
public class TimRefreshConfiguration {

    private TimeToLive sdwTtl;
    private BigDecimal defaultLaneWidth;
    private String odeUrl;
    private String cvRestService;

    public TimeToLive getSdwTtl() {
        return sdwTtl;
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

    public BigDecimal getDefaultLaneWidth() {
        return defaultLaneWidth;
    }

    public void setDefaultLaneWidth(BigDecimal defaultLaneWidth) {
        this.defaultLaneWidth = defaultLaneWidth;
    }

    public void setSdwTtl(String sdwTtl) {
        this.sdwTtl = TimeToLive.valueOf(sdwTtl);
    }
}