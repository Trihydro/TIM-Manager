package com.trihydro.library.model;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class WydotTim implements Cloneable {

	@ApiModelProperty(required = true)
	private String direction;
	@ApiModelProperty(required = true)
	private Coordinate startPoint;
	@ApiModelProperty(required = true)
	private Coordinate endPoint;
	@ApiModelProperty(required = true)
	private String route;
	@ApiModelProperty(required = true)
	private List<String> itisCodes;
	@ApiModelProperty(required = true)
	private String clientId;

	@Override
	public WydotTim clone() throws CloneNotSupportedException {
		return (WydotTim) super.clone();
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