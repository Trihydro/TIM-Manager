package com.trihydro.odewrapper.model;

import java.util.List;

public class WydotTim implements Cloneable {

	private String direction;
	private Double fromRm;
	private Double toRm;
	private String route;
	private List<String> itisCodes;
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

	public Double getFromRm() {
		return this.fromRm;
	}

	public void setFromRm(Double fromRm) {
		this.fromRm = fromRm;
	}

	public Double getToRm() {
		return this.toRm;
	}

	public void setToRm(Double toRm) {
		this.toRm = toRm;
	}

	public String getRoute() {
		return this.route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}