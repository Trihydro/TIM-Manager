package com.trihydro.library.model;

import java.sql.Timestamp;

public class TracMessageSent {

    private Integer tracMessageSentId;
    private Integer tracMessageTypeId;
    private Timestamp dateTimeSent;
    private String messageText;
    private String packetId;

    private Integer restResponseCode;
    private String restResponseMessage;
    private boolean messageSent;
    private boolean emailSent;

    public Integer getTracMessageSentId() {
        return this.tracMessageSentId;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    public boolean isMessageSent() {
        return messageSent;
    }

    public void setMessageSent(boolean messageSent) {
        this.messageSent = messageSent;
    }

    public String getRestResponseMessage() {
        return restResponseMessage;
    }

    public void setRestResponseMessage(String restResponseMessage) {
        this.restResponseMessage = restResponseMessage;
    }

    public Integer getRestResponseCode() {
        return restResponseCode;
    }

    public void setRestResponseCode(Integer restResponseCode) {
        this.restResponseCode = restResponseCode;
    }

    public void setTracMessageSentId(Integer tracMessageSentId) {
        this.tracMessageSentId = tracMessageSentId;
    }

    public Integer getTracMessageTypeId() {
        return this.tracMessageTypeId;
    }

    public void setTracMessageTypeId(Integer tracMessageTypeId) {
        this.tracMessageTypeId = tracMessageTypeId;
    }

    public Timestamp getDateTimeSent() {
        return this.dateTimeSent;
    }

    public void setDateTimeSent(Timestamp dateTimeSent) {
        this.dateTimeSent = dateTimeSent;
    }

    public String getMessageText() {
        return this.messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    
    public String getPacketId() {
        return this.packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }
}