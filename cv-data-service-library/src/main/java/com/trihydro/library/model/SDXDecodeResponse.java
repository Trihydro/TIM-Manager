package com.trihydro.library.model;

public class SDXDecodeResponse {
    private String messageType;
    private String decodedMessage;

    public String getMessageType() {
        return messageType;
    }
    public String getDecodedMessage() {
        return decodedMessage;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    public void setDecodedMessage(String decodedMessage) {
        this.decodedMessage = decodedMessage;
    }
}