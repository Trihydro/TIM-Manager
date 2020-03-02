package com.trihydro.odewrapper.model;

public class Buffer {
	private Double distance;
	private String action;

	public Double getDistance() {
		return this.distance;
	}

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
