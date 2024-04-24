package com.trihydro.odewrapper.model;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotTim;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimBowr extends WydotTim {

    @ApiModelProperty(value = "Expected values are I, D, B", required = true)
	private String direction;

    @ApiModelProperty(value = "Expected value is BlowOverWeightRestriction", required = true)
	private String type;
    
    @ApiModelProperty(value = "The common name for the selected route", required = true)
	private String route;

    @ApiModelProperty(required = true)
	private String clientId;

    @ApiModelProperty(required = true)
	private Coordinate startPoint;

	@ApiModelProperty(required = true)
	private Coordinate endPoint;
    
	@ApiModelProperty(required = false)
	private String startDateTime;

    @ApiModelProperty(required = false)
	private String endDateTime;

    @ApiModelProperty(required = true)
	private int data;

    public WydotTimBowr() {

    }

    public WydotTimBowr(WydotTimBowr o) {
        super(o);
        this.direction = o.direction;
        this.type = o.type;
        this.route = o.route;
        this.clientId = o.clientId;
        this.startPoint = o.startPoint;
        this.endPoint = o.endPoint;
        this.startDateTime = o.startDateTime;
        this.endDateTime = o.endDateTime;
        this.data = o.data;
    }

    public WydotTimBowr copy() {
        return new WydotTimBowr(this);
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Coordinate getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    public Coordinate getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Coordinate endPoint) {
        this.endPoint = endPoint;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}