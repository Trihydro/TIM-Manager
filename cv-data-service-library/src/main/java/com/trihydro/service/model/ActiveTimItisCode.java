package com.trihydro.service.model;

public class ActiveTimItisCode {

    private Long activeTimItisCodeId;        
    private Long activeTimId;
    private Long itisCodeId;
    
    public Long getActiveTimId() {
        return this.activeTimId;
    }

    public void setActiveTimId(Long activeTimId) {
        this.activeTimId = activeTimId;
    }

    public Long getActiveTimItisCodeId() {
        return this.activeTimItisCodeId;
    }

    public void setActiveTimItisCodeId(Long activeTimItisCodeId) {
        this.activeTimItisCodeId = activeTimItisCodeId;
    }

    public Long getItisCodeId() {
        return this.itisCodeId;
    }

    public void setItisCodeId(Long itisCodeId) {
        this.itisCodeId = itisCodeId;
    }
}