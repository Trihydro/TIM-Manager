package com.trihydro.cvdatacontroller.model;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.CountyRoadsProps;
import com.trihydro.library.model.DbInteractionsProps;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.model.JCSCacheProps;
import com.trihydro.library.tables.LoggingTables;
import com.trihydro.library.tables.TimDbTables;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
@Import({ TimDbTables.class, SQLNullHandler.class, Utility.class, EmailHelper.class,
        JavaMailSenderImplProvider.class, LoggingTables.class, DbInteractions.class })
public class DataControllerConfigProperties implements DbInteractionsProps, EmailProps, JCSCacheProps, CountyRoadsProps {
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private String dbUrlCountyRoads;
    private String dbUsernameCountyRoads;
    private String dbPasswordCountyRoads;

    private int maximumPoolSize;
    private int connectionTimeout;

    private String[] alertAddresses;
    private String fromEmail;
    private String environmentName;
    private String mailHost;
    private int mailPort;

    private String jcsDefault;
    private String cacheAttributes;
    private String maxObjects;
    private String memoryCacheName;
    private String useMemoryShrinker;
    private String maxMemoryIdleTimeSeconds;
    private String shrinkerIntervalSeconds;
    private String maxSpoolPerRun;
    private String elementAttributes;
    private String isEternal;
    private String maxLife;
    private String isSpool;
    private String isRemote;
    private String isLateral;

    private String countyRoadsTriggerViewName;
    private String countyRoadsGeometryViewName;
    private String countyRoadsReportViewName;
    private String countyRoadsWtiSectionsViewName;

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUrlCountyRoads() {
        return dbUrlCountyRoads;
    }

    public void setDbUrlCountyRoads(String dbUrlCountyRoads) {
        this.dbUrlCountyRoads = dbUrlCountyRoads;
    }

    public String getDbUsernameCountyRoads() {
        return dbUsernameCountyRoads;
    }

    public void setDbUsernameCountyRoads(String dbUsernameCountyRoads) {
        this.dbUsernameCountyRoads = dbUsernameCountyRoads;
    }

    public String getDbPasswordCountyRoads() {
        return dbPasswordCountyRoads;
    }

    public void setDbPasswordCountyRoads(String dbPasswordCountyRoads) {
        this.dbPasswordCountyRoads = dbPasswordCountyRoads;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
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

    @Override
    public String getJcsDefault() {
        return jcsDefault;
    }

    @Override
    public void setJcsDefault(String jcsDefault) {
        this.jcsDefault = jcsDefault;
    }

    @Override
    public String getCacheAttributes() {
        return cacheAttributes;
    }

    @Override
    public void setCacheAttributes(String cacheAttributes) {
        this.cacheAttributes = cacheAttributes;
    }

    @Override
    public String getMaxObjects() {
        return maxObjects;
    }

    @Override
    public void setMaxObjects(String maxObjects) {
        this.maxObjects = maxObjects;
    }

    @Override
    public String getMemoryCacheName() {
        return memoryCacheName;
    }

    @Override
    public void setMemoryCacheName(String memoryCacheName) {
        this.memoryCacheName = memoryCacheName;
    }

    @Override
    public String getUseMemoryShrinker() {
        return useMemoryShrinker;
    }

    @Override
    public void setUseMemoryShrinker(String useMemoryShrinker) {
        this.useMemoryShrinker = useMemoryShrinker;
    }

    @Override
    public String getMaxMemoryIdleTimeSeconds() {
        return maxMemoryIdleTimeSeconds;
    }

    @Override
    public void setMaxMemoryIdleTimeSeconds(String maxMemoryIdleTimeSeconds) {
        this.maxMemoryIdleTimeSeconds = maxMemoryIdleTimeSeconds;
    }

    @Override
    public String getShrinkerIntervalSeconds() {
        return shrinkerIntervalSeconds;
    }

    @Override
    public void setShrinkerIntervalSeconds(String shrinkerIntervalSeconds) {
        this.shrinkerIntervalSeconds = shrinkerIntervalSeconds;
    }

    @Override
    public String getMaxSpoolPerRun() {
        return maxSpoolPerRun;
    }

    @Override
    public void setMaxSpoolPerRun(String maxSpoolPerRun) {
        this.maxSpoolPerRun = maxSpoolPerRun;
    }

    @Override
    public String getElementAttributes() {
        return elementAttributes;
    }

    @Override
    public void setElementAttributes(String elementAttributes) {
        this.elementAttributes = elementAttributes;
    }

    @Override
    public String getIsEternal() {
        return isEternal;
    }

    @Override
    public void setIsEternal(String isEternal) {
        this.isEternal = isEternal;
    }

    @Override
    public String getMaxLife() {
        return maxLife;
    }

    @Override
    public void setMaxLife(String maxLife) {
        this.maxLife = maxLife;
    }

    @Override
    public String getIsSpool() {
        return isSpool;
    }

    @Override
    public void setIsSpool(String isSpool) {
        this.isSpool = isSpool;
    }

    @Override
    public String getIsRemote() {
        return isRemote;
    }

    @Override
    public void setIsRemote(String isRemote) {
        this.isRemote = isRemote;
    }

    @Override
    public String getIsLateral() {
        return isLateral;
    }

    @Override
    public void setIsLateral(String isLateral) {
        this.isLateral = isLateral;
    }

    @Override
    public String getCountyRoadsGeometryViewName() {
        return countyRoadsGeometryViewName;
    }

    @Override
    public void setCountyRoadsGeometryViewName(String countyRoadsGeometryViewName) {
        this.countyRoadsGeometryViewName = countyRoadsGeometryViewName;
    }

    @Override
    public String getCountyRoadsReportViewName() {
        return countyRoadsReportViewName;
    }

    @Override
    public void setCountyRoadsReportViewName(String countyRoadsReportViewName) {
        this.countyRoadsReportViewName = countyRoadsReportViewName;
    }

    @Override
    public String getCountyRoadsWtiSectionsViewName() {
        return countyRoadsWtiSectionsViewName;
    }

    @Override
    public void setCountyRoadsWtiSectionsViewName(String countyRoadsWtiSectionsViewName) {
        this.countyRoadsWtiSectionsViewName = countyRoadsWtiSectionsViewName;
    }
}