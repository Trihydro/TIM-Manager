package com.trihydro.adhoclistener.config;

import com.trihydro.library.model.DbInteractionsProps;
import com.trihydro.library.model.EmailProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class AdhocListenerConfiguration implements DbInteractionsProps, EmailProps {
    private String depositGroup;
    private String depositTopic;
    private String kafkaHostServer;
    private int maxPollIntervalMs = 300000;
    private int maxPollRecords = 50;

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

    private String listenerType;
    private String odeWrapperRestService;

    public String getDepositGroup() {
        return depositGroup;
    }

    public void setDepositGroup(String depositGroup) {
        this.depositGroup = depositGroup;
    }

    public String getDepositTopic() {
        return depositTopic;
    }

    public void setDepositTopic(String depositTopic) {
        this.depositTopic = depositTopic;
    }

    public String getKafkaHostServer() {
        return kafkaHostServer;
    }

    public void setKafkaHostServer(String hostname) {
        this.kafkaHostServer = hostname;
    }

    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs;
    }

    public void setMaxPollIntervalMs(int maxPollIntervalMs) {
        this.maxPollIntervalMs = maxPollIntervalMs;
    }

    public int getMaxPollRecords() {
        return maxPollRecords;
    }

    public void setMaxPollRecords(int maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }

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

    public String getListenerType() {
        return listenerType;
    }

    public void setListenerType(String listenerType) {
        this.listenerType = listenerType;
    }

    public String getOdeWrapperRestService() {
        return odeWrapperRestService;
    }

    public void setOdeWrapperRestService(String odeWrapperRestService) {
        this.odeWrapperRestService = odeWrapperRestService;
    }
}