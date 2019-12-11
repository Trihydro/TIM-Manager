package com.trihydro.library.model;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;

public class WydotRsu extends RSU
{
	private static final long serialVersionUID = 3381208236984831107L;
	private Double latitude;
	private Double longitude;
	private Integer rsuId;
	private String route;
	private Double milepost;
	
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

	public String getRoute() 
	{
		return this.route;
	}

	public void setRoute(String route) 
	{
		this.route = route;
	}

	public Double getMilepost() 
	{
		return this.milepost;
	}

	public void setMilepost(Double milepost) 
	{
		this.milepost = milepost;
	}
}