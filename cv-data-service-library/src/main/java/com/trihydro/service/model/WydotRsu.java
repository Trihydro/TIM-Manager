package com.trihydro.service.model;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;

public class WydotRsu extends RSU
{
	private Double latitude;
	private Double longitude;
	private Integer rsuId;
	
	public Integer getRsuId() 
	{
		return this.rsuId;
	}

	public void setRsuId(Integer rsuId) 
	{
		this.rsuId = rsuId;
	}

	public Double getLatitude() 
	{
		return this.latitude;
	}

	public void setLatitude(Double latitude) 
	{
		this.latitude = latitude;
	}

	public Double getLongitude() 
	{
		return this.longitude;
	}

	public void setLongitude(Double longitude) 
	{
		this.longitude = longitude;
	}
}