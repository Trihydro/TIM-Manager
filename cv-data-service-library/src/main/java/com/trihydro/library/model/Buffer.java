package com.trihydro.library.model;

import io.swagger.annotations.ApiModelProperty;

public class Buffer {
	private Double distance;
	
	@ApiModelProperty(value = "Expected values are leftClosed, rightClosed, workers, surfaceGravel, surfaceMilled, surfaceDirt, delay, prepareStop")
	private String action;

	public Buffer() {

	}

	public Buffer(Buffer o) {
		this.distance = o.distance;
		this.action = o.action;
	}

	public Double getDistance() {
		return this.distance;
	}

	@ApiModelProperty(hidden = true)
	public Double getDistanceMeters() {
		return this.distance * 1609.34;

	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
