package com.trihydro.loggerkafkaconsumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class LoggerConfiguration {

    private String depositGroup;
    private String depositTopic;
    private String kafkaHostServer;
    private String dbUsername;
    private String dbPassword;
    private String dbUrl;
    private String dbDriver;
    private String[] alertAddresses;
    private String fromEmail;
    private String mailHost;
    private int mailPort;

    public String getDepositGroup() {
        return depositGroup;
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

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
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

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getKafkaHostServer() {
        return kafkaHostServer;
    }

    public void setKafkaHostServer(String hostname) {
        this.kafkaHostServer = hostname;
    }

    public String getDepositTopic() {
        return depositTopic;
    }

    public void setDepositTopic(String depositTopic) {
        this.depositTopic = depositTopic;
    }

    public void setDepositGroup(String depositGroup) {
        this.depositGroup = depositGroup;
    }

}