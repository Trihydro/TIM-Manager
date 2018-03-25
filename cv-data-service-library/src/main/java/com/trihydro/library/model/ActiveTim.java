package com.trihydro.library.model;

public class ActiveTim {
    private Long activeTimId;
    private Long timId;
    private String recordId;    

    public Long getActiveTimId() {
        return this.activeTimId;
    }

    public void setActiveTimId(Long activeTimId) {
        this.activeTimId = activeTimId;
    }

    public Long getTimId() {
        return this.timId;
    }

    public void setTimId(Long timId) {
        this.timId = timId;
    }

    public String getRecordId() {
        return this.recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

}