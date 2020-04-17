package com.trihydro.library.model;

public class ConfigProperties implements SdwProps, RsuDataServiceProps, TmddProps {

    private String odeUrl;
    private String hostname;

    private String sdwRestUrl;
    private String sdwApiKey;

    private String mailHost;
    private int mailPort;

    private String[] alertAddresses;
    private String fromEmail;

    private String depositTopic;
    private String depositGroup;
    private String rsuDataServiceUrl;

    private String tmddUrl;
    private String tmddUser;
    private String tmddPassword;

    public String getOdeUrl() {
        return odeUrl;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public String getSdwApiKey() {
        return sdwApiKey;
    }

    public void setSdwApiKey(String sdwApiKey) {
        this.sdwApiKey = sdwApiKey;
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

    public String getSdwRestUrl() {
        return sdwRestUrl;
    }

    public void setSdwRestUrl(String sdwRestUrl) {
        this.sdwRestUrl = sdwRestUrl;
    }

    public void setOdeUrl(String odeUrl) {
        this.odeUrl = odeUrl;
    }

    public String getRsuDataServiceUrl() {
        return rsuDataServiceUrl;
    }

    public void setRsuDataServiceUrl(String rsuDataServiceUrl) {
        this.rsuDataServiceUrl = rsuDataServiceUrl;
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
}