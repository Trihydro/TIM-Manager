package com.trihydro.service.model;

import java.sql.Timestamp;

public class TracMessageSent {

    private Integer tracMessageSentId;
    private Integer tracMessageTypeId;
    private Timestamp dateTimeSent;
    private String messageText;
    private String packetId;

    public Integer getTracMessageSentId() {
        return this.tracMessageSentId;
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