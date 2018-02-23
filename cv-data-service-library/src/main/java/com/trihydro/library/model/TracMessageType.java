package com.trihydro.library.model;

public class TracMessageType {

    private Integer tracMessageTypeId;
    private String tracMessageType;
    private String tracMessageDescription;

    public Integer getTracMessageTypeId() {
        return this.tracMessageTypeId;
    }

    public void setTracMessageTypeId(Integer tracMessageTypeId) {
        this.tracMessageTypeId = tracMessageTypeId;
    }

    public String getTracMessageType() {
        return this.tracMessageType;
    }

    public void setTracMessageType(String tracMessageType) {
        this.tracMessageType = tracMessageType;
    }
    
    public String getTracMessageDescription() {
        return this.tracMessageDescription;
    }

    public void setTracMessageDescription(String tracMessageDescription) {
        this.tracMessageDescription = tracMessageDescription;
    }
}