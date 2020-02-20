package com.trihydro.timrefresh.config;

import java.math.BigDecimal;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

@Component
@ConfigurationProperties("config")
@ComponentScan({ "com.trihydro.timrefresh", "com.trihydro.library.service" })
public class TimRefreshConfiguration extends ConfigProperties {

    private TimeToLive sdwTtl;
    private BigDecimal defaultLaneWidth;
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