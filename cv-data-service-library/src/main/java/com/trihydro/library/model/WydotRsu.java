package com.trihydro.library.model;

import java.math.BigDecimal;

import us.dot.its.jpo.ode.plugin.SnmpProtocol;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;

public class WydotRsu extends RSU
{
	private static final long serialVersionUID = 3381208236984831107L;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private Integer rsuId;
	private String route;
	private Double milepost;

	public WydotRsu() 
	{
		super();
		setSnmpProtocol(SnmpProtocol.NTCIP1218);
	}
	
	public Integer getRsuId() 
	{
		return this.rsuId;
	}

	public void setRsuId(Integer rsuId) 
	{
		this.rsuId = rsuId;
	}

	public BigDecimal getLatitude() 
	{
		return this.latitude;
	}

	public void setLatitude(BigDecimal latitude) 
	{
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() 
	{
		return this.longitude;
	}

	public void setLongitude(BigDecimal longitude) 
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