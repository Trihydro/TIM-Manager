package com.trihydro.library.model;

public class SDXDecodeRequest {
    private String messageType;
    private String encodeType;
    private String encodedMsg;

    public String getMessageType() {
        return messageType;
    }
    public String getEncodeType() {
        return encodeType;
    }
    public String getEncodedMsg() {
        return encodedMsg;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    public void setEncodeType(String encodeType) {
        this.encodeType = encodeType;
    }
    public void setEncodedMsg(String encodedMsg) {
        this.encodedMsg = encodedMsg;
    }
}