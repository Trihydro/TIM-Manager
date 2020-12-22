package com.trihydro.tasks.config;

import java.math.BigDecimal;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.RsuDataServiceProps;
import com.trihydro.library.model.SdwProps;
import com.trihydro.library.model.TmddProps;
import com.trihydro.library.service.TimGenerationProps;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

@Component
@ConfigurationProperties("config")
public class DataTasksConfiguration
        implements SdwProps, RsuDataServiceProps, CVRestServiceProps, TmddProps, EmailProps, TimGenerationProps, OdeProps {

    private String cvRestService;
    private String rsuDataServiceUrl;
    private String wrapperUrl;
    private String sdwRestUrl;
    private String sdwApiKey;
    private String odeUrl;
    private String tmddUrl;
    private String tmddUser;
    private String tmddPassword;
    private String[] alertAddresses;
    private String fromEmail;
    private String environmentName;
    private String mailHost;
    private int mailPort;
    private int bsmRetentionPeriodDays;
    private boolean runTmddValidation;
    private boolean runRsuValidation;
    private int rsuValidationDelaySeconds = 60;
    private int rsuValThreadPoolSize = 1;
    private int rsuValTimeoutSeconds = 300; // 76 RSUs, 20s timeout each... Could still finish processing with up to 20%
                                            // of RSUs down in a pool w/ single thread

    private int removeExpiredPeriodMinutes = 1440;
    private int cleanupPeriodMinutes = 1440;
    private int sdxValidationPeriodMinutes = 1440;
    private int rsuValidationPeriodMinutes = 1440;
    private int tmddValidationPeriodMinutes = 1440;
    private int retentionEnforcementPeriodMinutes = 1440;// run once a day by default
    private int bsmCleanupPeriodMinutes = 1440;
    private int hsmFunctionalityMinutes = 1;// run once a minute by default
    private String hsmUrl = "http://10.145.9.74:55443/tmc";
    private int hsmErrorEmailFrequencyMinutes = 10;// send an email every 10 minutes the system is down
    private boolean runHsmCheck;

    private TimeToLive sdwTtl;
    private BigDecimal defaultLaneWidth = BigDecimal.valueOf(50);
    private String[] rsuRoutes;
    private Double pointIncidentBufferMiles;

    /**
     * Returns the defaultLaneWidth / 2
     *
     * @return
     */
    public Double getPathDistanceLimit() {
        return defaultLaneWidth.divide(BigDecimal.valueOf(2)).doubleValue();
    }

    public String getCvRestService() {
        return cvRestService;
    }

    public Double getPointIncidentBufferMiles() {
        return pointIncidentBufferMiles;
    }

    public void setPointIncidentBufferMiles(Double pointIncidentBufferMiles) {
        this.pointIncidentBufferMiles = pointIncidentBufferMiles;
    }

    public String[] getRsuRoutes() {
        return rsuRoutes;
    }

    public void setRsuRoutes(String[] rsuRoutes) {
        this.rsuRoutes = rsuRoutes;
    }

    public BigDecimal getDefaultLaneWidth() {
        return defaultLaneWidth;
    }

    public void setDefaultLaneWidth(BigDecimal defaultLaneWidth) {
        this.defaultLaneWidth = defaultLaneWidth;
    }

    public TimeToLive getSdwTtl() {
        return sdwTtl;
    }

    public void setSdwTtl(TimeToLive sdwTtl) {
        this.sdwTtl = sdwTtl;
    }

    public int getRetentionEnforcementPeriodMinutes() {
        return retentionEnforcementPeriodMinutes;
    }

    public void setRetentionEnforcementPeriodMinutes(int retentionEnforcementPeriodMinutes) {
        this.retentionEnforcementPeriodMinutes = retentionEnforcementPeriodMinutes;
    }

    public void setCvRestService(String cvRestService) {
        this.cvRestService = cvRestService;
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

    public String getOdeUrl() {
        return odeUrl;
    }

    public void setOdeUrl(String odeUrl) {
        this.odeUrl = odeUrl;
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

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
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

    public int getBsmRetentionPeriodDays() {
        return bsmRetentionPeriodDays;
    }

    public void setBsmRetentionPeriodDays(int bsmRetentionPeriodDays) {
        this.bsmRetentionPeriodDays = bsmRetentionPeriodDays;
    }

    public boolean getRunTmddValidation() {
        return runTmddValidation;
    }

    public void setRunTmddValidation(boolean runTmddValidation) {
        this.runTmddValidation = runTmddValidation;
    }

    public boolean getRunRsuValidation() {
        return runRsuValidation;
    }

    public void setRunRsuValidation(boolean runRsuValidation) {
        this.runRsuValidation = runRsuValidation;
    }

    public int getRsuValidationDelaySeconds() {
        return rsuValidationDelaySeconds;
    }

    public void setRsuValidationDelaySeconds(int rsuValidationDelaySeconds) {
        this.rsuValidationDelaySeconds = rsuValidationDelaySeconds;
    }

    public int getRsuValThreadPoolSize() {
        return rsuValThreadPoolSize;
    }

    public void setRsuValThreadPoolSize(int rsuValThreadPoolSize) {
        if (rsuValThreadPoolSize > 0) {
            this.rsuValThreadPoolSize = rsuValThreadPoolSize;
        }
    }

    public int getRsuValTimeoutSeconds() {
        return rsuValTimeoutSeconds;
    }

    public void setRsuValTimeoutSeconds(int rsuValTimeoutSeconds) {
        if (rsuValTimeoutSeconds > 0) {
            this.rsuValTimeoutSeconds = rsuValTimeoutSeconds;
        }
    }

    public int getRemoveExpiredPeriodMinutes() {
        return removeExpiredPeriodMinutes;
    }

    public void setRemoveExpiredPeriodMinutes(int removeExpiredPeriodMinutes) {
        if (removeExpiredPeriodMinutes > 0) {
            this.removeExpiredPeriodMinutes = removeExpiredPeriodMinutes;
        }
    }

    public int getCleanupPeriodMinutes() {
        return cleanupPeriodMinutes;
    }

    public void setCleanupPeriodMinutes(int cleanupPeriodMinutes) {
        if (cleanupPeriodMinutes > 0) {
            this.cleanupPeriodMinutes = cleanupPeriodMinutes;
        }
    }

    public int getSdxValidationPeriodMinutes() {
        return sdxValidationPeriodMinutes;
    }

    public void setSdxValidationPeriodMinutes(int sdxValidationPeriodMinutes) {
        if (sdxValidationPeriodMinutes > 0) {
            this.sdxValidationPeriodMinutes = sdxValidationPeriodMinutes;
        }
    }

    public int getRsuValidationPeriodMinutes() {
        return rsuValidationPeriodMinutes;
    }

    public void setRsuValidationPeriodMinutes(int rsuValidationPeriodMinutes) {
        if (rsuValidationPeriodMinutes > 0) {
            this.rsuValidationPeriodMinutes = rsuValidationPeriodMinutes;
        }
    }

    public String getTmddUrl() {
        return tmddUrl;
    }

    public void setTmddUrl(String tmddUrl) {
        this.tmddUrl = tmddUrl;
    }

    public String getTmddUser() {
        return tmddUser;
    }

    public void setTmddUser(String tmddUser) {
        this.tmddUser = tmddUser;
    }

    public String getTmddPassword() {
        return tmddPassword;
    }

    public void setTmddPassword(String tmddPassword) {
        this.tmddPassword = tmddPassword;
    }

    public int getTmddValidationPeriodMinutes() {
        return tmddValidationPeriodMinutes;
    }

    public void setTmddValidationPeriodMinutes(int tmddValidationPeriodMinutes) {
        this.tmddValidationPeriodMinutes = tmddValidationPeriodMinutes;
    }

    public int getBsmCleanupPeriodMinutes() {
        return bsmCleanupPeriodMinutes;
    }

    public void setBsmCleanupPeriodMinutes(int bsmCleanupPeriodMinutes) {
        this.bsmCleanupPeriodMinutes = bsmCleanupPeriodMinutes;
    }

    public int getHsmFunctionalityMinutes() {
        return hsmFunctionalityMinutes;
    }

    public void setHsmFunctionalityMinutes(int hsmFunctionalityMinutes) {
        this.hsmFunctionalityMinutes = hsmFunctionalityMinutes;
    }

    public String getHsmUrl() {
        return hsmUrl;
    }

    public void setHsmUrl(String hsmUrl) {
        this.hsmUrl = hsmUrl;
    }

    public int getHsmErrorEmailFrequencyMinutes() {
        return hsmErrorEmailFrequencyMinutes;
    }

    public void setHsmErrorEmailFrequencyMinutes(int hsmErrorEmailFrequencyMinutes) {
        this.hsmErrorEmailFrequencyMinutes = hsmErrorEmailFrequencyMinutes;
    }

    public boolean getRunHsmCheck() {
        return runHsmCheck;
    }

    public void setRunHsmCheck(boolean runHsmCheck) {
        this.runHsmCheck = runHsmCheck;
    }
}