package com.trihydro.mongologger.app;

import com.trihydro.library.model.EmailProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mongologger")
public class MongoLoggerConfiguration implements EmailProps {
    private String mongoDatabase;
    private String mongoAuthDatabase;
    private String mongoUsername;
    private String mongoPassword;
    private String hostname;
    private String mongoHost;
    private String mailHost;
    private int mailPort;

    private String[] alertAddresses;
    private String fromEmail;
    private String environmentName;

    private String depositTopic;
    private String depositGroup;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public String getMongoAuthDatabase() {
        return mongoAuthDatabase;
    }

    public void setMongoAuthDatabase(String mongoAuthDatabase) {
        this.mongoAuthDatabase = mongoAuthDatabase;
    }
}
