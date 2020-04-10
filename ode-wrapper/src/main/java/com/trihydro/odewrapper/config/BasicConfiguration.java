package com.trihydro.odewrapper.config;

import java.math.BigDecimal;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

@Component
@ConfigurationProperties("config")
public class BasicConfiguration extends ConfigProperties implements CVRestServiceProps {

    private TimeToLive sdwTtl;
    private BigDecimal defaultLaneWidth;
    private String cvRestService;
    private String[] rsuRoutes;

    public TimeToLive getSdwTtl() {
        return sdwTtl;
    }

    public String[] getRsuRoutes() {
        return rsuRoutes;
    }

    public void setRsuRoutes(String[] rsuRoutes) {
        this.rsuRoutes = rsuRoutes;
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