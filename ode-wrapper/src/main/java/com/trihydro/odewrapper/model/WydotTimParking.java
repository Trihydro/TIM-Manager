package com.trihydro.odewrapper.model;

import java.util.ArrayList;
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

    public WydotTimParking() {

    }

    public WydotTimParking(WydotTimParking o) {
        super(o);
        this.mileMarker = o.mileMarker;
        this.availability = o.availability;
        this.exit = o.exit;

        if (o.itisCodes != null)
            this.itisCodes = new ArrayList<>(o.itisCodes);
        if (o.endPoint != null)
            this.endPoint = new Coordinate(o.endPoint.getLatitude(), o.endPoint.getLongitude());
    }

    @Override
    public WydotTimParking copy() {
        return new WydotTimParking(this);
    }

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