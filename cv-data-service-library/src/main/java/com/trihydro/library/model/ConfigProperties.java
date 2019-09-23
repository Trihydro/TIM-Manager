package com.trihydro.library.model;

public class ConfigProperties {

    private String odeUrl;
    private String dbDriver;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String env;
    private String mongoDatabase;
    private String mongoUsername;
    private String mongoPassword;
    private String mongoHost;
    private String trackUrl;

    private String sdwRestUrl;
    private String sdwUsername;
    private String sdwPassword;

    private String mailHost;
    private int mailPort;

    private String[] alertAddresses;

    public String getOdeUrl() {
        return odeUrl;
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

    public String[] getAlertAddresses() {
        return alertAddresses;
    }

    public void setAlertAddresses(String[] alertAddresses) {
        this.alertAddresses = alertAddresses;
    }

    public void setAlertAddresses(String alertAddresses) {
        this.alertAddresses = alertAddresses.split(",");
    }

    public String getSdwPassword() {
        return sdwPassword;
    }

    public void setSdwPassword(String sdwPassword) {
        this.sdwPassword = sdwPassword;
    }

    public String getSdwUsername() {
        return sdwUsername;
    }

    public void setSdwUsername(String sdwUsername) {
        this.sdwUsername = sdwUsername;
    }

    public String getSdwRestUrl() {
        return sdwRestUrl;
    }

    public void setSdwRestUrl(String sdwRestUrl) {
        this.sdwRestUrl = sdwRestUrl;
    }

    public void setOdeUrl(String odeUrl) {
        this.odeUrl = odeUrl;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
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

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public String getMongoUsername() {
        return mongoUsername;
    }

    public void setMongoUsername(String mongoUsername) {
        this.mongoUsername = mongoUsername;
    }

    public String getMongoPassword() {
        return mongoPassword;
    }

    public void setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
    }

    public String getMongoHost() {
        return mongoHost;
    }

    public void setMongoHost(String mongoHost) {
        this.mongoHost = mongoHost;
    }

    public void setTracUrl(String url) {
        this.trackUrl = url;
    }

    public String getGetTrackUrl() {
        return this.trackUrl;
    }
}