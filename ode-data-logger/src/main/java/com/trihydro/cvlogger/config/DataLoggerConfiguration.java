package com.trihydro.cvlogger.config;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class DataLoggerConfiguration extends ConfigProperties {
    private String kafkaHostServer;
    private String tracUrl;

    private String mailHost;
    private int mailPort;

    private String[] alertAddresses;
    private String fromEmail;

    private String depositTopic;
    private String depositGroup;
    private String cvRestService;

    private String producerTopic;

    public String getTracUrl() {
        return tracUrl;
    }

    public String getProducerTopic() {
        return producerTopic;
    }

    public void setProducerTopic(String producerTopic) {
        this.producerTopic = producerTopic;
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

    public String getKafkaHostServer() {
        return kafkaHostServer;
    }

    public void setKafkaHostServer(String kafkaHostServer) {
        this.kafkaHostServer = kafkaHostServer;
    }

    public String getCvRestService() {
        return cvRestService;
    }

    public void setCvRestService(String cvRestService) {
        this.cvRestService = cvRestService;
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

    public void setTracUrl(String tracUrl) {
        this.tracUrl = tracUrl;
    }
}