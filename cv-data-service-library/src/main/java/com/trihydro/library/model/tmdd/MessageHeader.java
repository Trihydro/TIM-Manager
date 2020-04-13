package com.trihydro.library.model.tmdd;

public class MessageHeader {
    private OrganizationInformation organizationSending;
    private Integer messageTypeVersion;
    private Integer messageNumber;
    private DateTimeZone messageTimeStamp;
    private DateTimeZone messageExpiryTime;

    public OrganizationInformation getOrganizationSending() {
        return organizationSending;
    }

    public void setOrganizationSending(OrganizationInformation organizationSending) {
        this.organizationSending = organizationSending;
    }

    public Integer getMessageTypeVersion() {
        return messageTypeVersion;
    }

    public void setMessageTypeVersion(Integer messageTypeVersion) {
        this.messageTypeVersion = messageTypeVersion;
    }

    public Integer getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(Integer messageNumber) {
        this.messageNumber = messageNumber;
    }

    public DateTimeZone getMessageTimeStamp() {
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(DateTimeZone messageTimeStamp) {
        this.messageTimeStamp = messageTimeStamp;
    }

    public DateTimeZone getMessageExpiryTime() {
        return messageExpiryTime;
    }

    public void setMessageExpiryTime(DateTimeZone messageExpiryTime) {
        this.messageExpiryTime = messageExpiryTime;
    }
}
