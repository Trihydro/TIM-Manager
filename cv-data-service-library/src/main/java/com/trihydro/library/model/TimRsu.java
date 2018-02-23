package com.trihydro.library.model;

public class TimRsu {
    private Long timId;
    private Long rsuId;   
    private Long timRsuId; 

    public Long getTimRsuId() {
        return this.timRsuId;
    }

    public void setTimRsuId(Long timRsuId) {
        this.timRsuId = timRsuId;
    }

    public Long getTimId() {
        return this.timId;
    }

    public void setTimId(Long timId) {
        this.timId = timId;
    }

    public Long getRsuId() {
        return this.rsuId;
    }

    public void setRsuId(Long rsuId) {
        this.rsuId = rsuId;
    }

}