package com.trihydro.odewrapper.model;

public class WydotTimRc extends WydotTim {

    private String roadCode; // for road condition TIMs
    private Integer[] advisory;
    private String segment; // for chain law TIMs

    public String getRoadCode() {
        return this.roadCode;
    }

    public void setRoadCode(String roadCode) {
        this.roadCode = roadCode;
    }

    public String getSegment() {
        return this.segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Integer[] getAdvisory() {
        return this.advisory;
    }

    public void setAdvisory(Integer[] advisory) {
        this.advisory = advisory;
    }
}