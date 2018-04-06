package com.trihydro.library.model;

public class ActiveTim {
    private Long activeTimId;
    private Long timId;
    private String recordId;    
    private Double milepostStart;
    private Double milepostStop;
    private String timType;

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

    public Double getMilepostStart() {
        return this.milepostStart;
    }

    public void setMilepostStart(Double milepostStart) {
        this.milepostStart = milepostStart;
    }

    public Double getMilepostStop() {
        return this.milepostStop;
    }

    public void setMilepostStop(Double milepostStop) {
        this.milepostStop = milepostStop;
    }

    public String getTimType() {
        return this.timType;
    }

    public void setTimType(String timType) {
        this.timType = timType;
    }

}