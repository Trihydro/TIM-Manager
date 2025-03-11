package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class WydotTim {

    @ApiModelProperty(value = "Expected values are I, D, B", required = true)
    private String direction;
    @ApiModelProperty(required = true)
    private Coordinate startPoint;
    @ApiModelProperty(required = true)
    private Coordinate endPoint;
    @ApiModelProperty(value = "The common name for the selected route", required = true)
    private String route;
    @ApiModelProperty(required = true)
    private List<String> itisCodes;
    @ApiModelProperty(required = true)
    private String clientId;

    public WydotTim() {

    }

    public WydotTim(WydotTim o) {
        this.direction = o.direction;
        if (o.startPoint != null) {
            this.startPoint =
                new Coordinate(o.startPoint.getLatitude(), o.startPoint.getLongitude());
        }
        if (o.endPoint != null) {
            this.endPoint = new Coordinate(o.endPoint.getLatitude(), o.endPoint.getLongitude());
        }
        this.route = o.route;
        if (o.itisCodes != null) {
            this.itisCodes = new ArrayList<>(o.itisCodes);
        }
        this.clientId = o.clientId;
    }

    public WydotTim(TimUpdateModel aTim) {
        setClientId(aTim.getClientId());
        setDirection(aTim.getDirection());
        setStartPoint(aTim.getStartPoint());
        setEndPoint(aTim.getEndPoint());
        setRoute(aTim.getRoute());
        for (Integer itisCode : aTim.getItisCodes()) {
            this.itisCodes.add(itisCode.toString());
        }
    }

    public WydotTim copy() {
        return new WydotTim(this);
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<String> getItisCodes() {
        return this.itisCodes;
    }

    public void setItisCodes(List<String> itisCodes) {
        this.itisCodes = itisCodes;
    }

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Coordinate getStartPoint() {
        return this.startPoint;
    }

    public void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    public Coordinate getEndPoint() {
        return this.endPoint;
    }

    public void setEndPoint(Coordinate endPoint) {
        this.endPoint = endPoint;
    }

    public String getRoute() {
        return this.route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}