package com.trihydro.odewrapper.model;

import java.util.List;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotTim;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimParking extends WydotTim {

    private Double mileMarker;
    private Integer availability;
    private String exit;

    @ApiModelProperty(hidden = true)
    private transient List<String> itisCodes;
    @ApiModelProperty(hidden = true)
	private transient Coordinate endPoint;

    public String getExit() {
        return this.exit;
    }

    public void setExit(String exit) {
        this.exit = exit;
    }

    public Integer getAvailability() {
        return this.availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    public Double getMileMarker() {
        return this.mileMarker;
    }

    public void setMileMarker(Double mileMarker) {
        this.mileMarker = mileMarker;
    }

}