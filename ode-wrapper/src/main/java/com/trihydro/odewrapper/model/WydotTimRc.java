package com.trihydro.odewrapper.model;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.WydotTim;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimRc extends WydotTim {

    @ApiModelProperty(value = "This parameter is only required for road condition TIMs", required = true)
    private String roadCode; // for road condition TIMs
    @ApiModelProperty(required = true, value = "Supported ITIS codes can be found in the Wyoming CV Pilot System Design Document, Appendix A")
    private Integer[] advisory;
    @ApiModelProperty(value = "This parameter is only required for chain law TIMs", required = true)
    private String segment; // for chain law TIMs
    @ApiModelProperty(hidden = true)
    private transient String clientId;
    @ApiModelProperty(hidden = true)
    private transient List<String> itisCodes;

    public WydotTimRc() {

    }

    public WydotTimRc(WydotTimRc o) {
        super(o);
        this.roadCode = o.roadCode;
        if (o.advisory != null)
            this.advisory = o.advisory.clone();
        this.segment = o.segment;
        this.clientId = o.clientId;

        if (o.itisCodes != null)
            this.itisCodes = new ArrayList<>(o.itisCodes);
    }

    @Override
    public WydotTimRc copy() {
        return new WydotTimRc(this);
    }

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