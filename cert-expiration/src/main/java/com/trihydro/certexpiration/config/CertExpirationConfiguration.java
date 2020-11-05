package com.trihydro.certexpiration.config;

import com.trihydro.library.model.CVRestServiceProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class CertExpirationConfiguration implements CVRestServiceProps {
    private String kafkaHostServer;

    private String mailHost;
    private int mailPort;

    private String[] alertAddresses;
    private String fromEmail;

    private String depositTopic;
    private String depositGroup;
    private String cvRestService;

    private int maxQueueSize;
    private long processWaitTime;

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

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public long getProcessWaitTime() {
        return processWaitTime;
    }

    private void setProcessWaitTime(long processWaitTime) {
        this.processWaitTime = processWaitTime;
    }
}