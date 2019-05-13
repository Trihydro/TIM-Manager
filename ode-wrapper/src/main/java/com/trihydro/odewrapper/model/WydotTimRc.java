package com.trihydro.odewrapper.model;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimRc extends WydotTim {

    @ApiModelProperty(value = "This parameter is only required for road condition TIMs", required = true)
    private String roadCode; // for road condition TIMs
    @ApiModelProperty(required = true)
    private Integer[] advisory;
    @ApiModelProperty(value = "This parameter is only required for chain law TIMs", required = true)
    private String segment; // for chain law TIMs
    @ApiModelProperty(hidden = true)
    private transient String clientId;
    @ApiModelProperty(hidden = true)
    private transient List<String> itisCodes;

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