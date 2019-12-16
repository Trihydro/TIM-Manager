package com.trihydro.odewrapper.model;

import java.util.List;

import com.trihydro.library.model.WydotTim;

public class WydotTimList {
	private List<WydotTimVsl> timVslList;
	private List<WydotTimCc> timCcList;
	private List<WydotTimRc> timRcList;
	private List<WydotTimRw> timRwList;
	private List<WydotTimParking> timParkingList;
	private List<WydotTimIncident> timIncidentList;
	private List<WydotTim> timList;

	public List<WydotTimVsl> getTimVslList() {
		return this.timVslList;
	}

	public void setTimVslList(List<WydotTimVsl> timVslList) {
		this.timVslList = timVslList;
	}

	public List<WydotTimCc> getTimCcList() {
		return this.timCcList;
	}

	public void setTimCcList(List<WydotTimCc> timCcList) {
		this.timCcList = timCcList;
	}

	public List<WydotTimRc> getTimRcList() {
		return this.timRcList;
	}

	public void setTimRcList(List<WydotTimRc> timRcList) {
		this.timRcList = timRcList;
	}

	public List<WydotTimRw> getTimRwList() {
		return this.timRwList;
	}

	public void setTimRwList(List<WydotTimRw> timRwList) {
		this.timRwList = timRwList;
	}

	public List<WydotTimParking> getTimParkingList() {
		return this.timParkingList;
	}

	public void setTimParkingList(List<WydotTimParking> timParkingList) {
		this.timParkingList = timParkingList;
	}

	public List<WydotTimIncident> getTimIncidentList() {
		return this.timIncidentList;
	}

	public void setTimIncidentList(List<WydotTimIncident> timIncidentList) {
		this.timIncidentList = timIncidentList;
	}

	public List<WydotTim> getTimList() {
		return this.timList;
	}

	public void setTimList(List<WydotTim> timList) {
		this.timList = timList;
	}
}