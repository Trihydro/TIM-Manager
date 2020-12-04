package com.trihydro.timrefresh.config;

import java.math.BigDecimal;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.SdwProps;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.PathNodeXYService;
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimGenerationProps;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

@Component
@ConfigurationProperties("config")
@Import({ ActiveTimHoldingService.class, ActiveTimService.class, DataFrameService.class, MilepostService.class,
        OdeService.class, PathNodeXYService.class, RegionService.class, RsuService.class, SdwService.class,
        Utility.class, RestTemplateProvider.class, MilepostReduction.class, JavaMailSenderImplProvider.class,
        EmailHelper.class,TimGenerationHelper.class })
public class TimRefreshConfiguration implements CVRestServiceProps, SdwProps, EmailProps, OdeProps, TimGenerationProps {

    private TimeToLive sdwTtl;
    private BigDecimal defaultLaneWidth = BigDecimal.valueOf(50);
    private String cvRestService;
    private String[] rsuRoutes;
    private String odeUrl;
    private String sdwRestUrl;
    private String sdwApiKey;
    private Double pointIncidentBufferMiles;

    private String mailHost;
    private int mailPort;

    private String[] alertAddresses;
    private String fromEmail;

    /**
     * Returns the defaultLaneWidth / 2
     * 
     * @return
     */
    public Double getPathDistanceLimit() {
        return defaultLaneWidth.divide(BigDecimal.valueOf(2)).doubleValue();
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String[] getAlertAddresses() {
        return alertAddresses;
    }

    public void setAlertAddresses(String[] alertAddresses) {
        this.alertAddresses = alertAddresses;
    }

    public int getMailPort() {
        return mailPort;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public TimeToLive getSdwTtl() {
        return sdwTtl;
    }

    public Double getPointIncidentBufferMiles() {
        return pointIncidentBufferMiles;
    }

    public void setPointIncidentBufferMiles(Double pointIncidentBufferMiles) {
        this.pointIncidentBufferMiles = pointIncidentBufferMiles;
    }

    @Override
    public String getSdwApiKey() {
        return sdwApiKey;
    }

    public void setSdwApiKey(String sdwApiKey) {
        this.sdwApiKey = sdwApiKey;
    }

    @Override
    public String getSdwRestUrl() {
        return sdwRestUrl;
    }

    public void setSdwRestUrl(String sdwRestUrl) {
        this.sdwRestUrl = sdwRestUrl;
    }

    public String getOdeUrl() {
        return odeUrl;
    }

    public void setOdeUrl(String odeUrl) {
        this.odeUrl = odeUrl;
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