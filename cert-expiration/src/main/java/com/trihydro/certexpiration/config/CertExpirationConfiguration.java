package com.trihydro.certexpiration.config;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.EmailProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class CertExpirationConfiguration implements CVRestServiceProps, EmailProps {
    private String kafkaHostServer;

    private String mailHost;
    private int mailPort;

    private String[] alertAddresses;
    private String fromEmail;
    private String environmentName;

    private String depositGroup;
    private String depositTopic;
    private String producerTopic;

    private String cvRestService;

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

    public String getProducerTopic() {
        return producerTopic;
    }

    public void setProducerTopic(String producerTopic) {
        this.producerTopic = producerTopic;
    }
}