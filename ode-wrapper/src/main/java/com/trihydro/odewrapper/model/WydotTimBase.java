package com.trihydro.odewrapper.model;

public class WydotTimBase
{
	private String route;
	private String direction;
	private Double fromRm;
	private Double toRm;

	public String getRoute() 
	{
		return this.route;
	}

	public void setRoute(String route) 
	{
		this.route = route;
	}

	public String getDirection() 
	{
		return this.direction;
	}

	public void setDirection(String direction) 
	{
		this.direction = direction;
	}

	public Double getFromRm() 
	{
		return this.fromRm;
	}

	public void setFromRm(Double fromRm) 
	{
		this.fromRm = fromRm;
	}

	public Double getToRm() 
	{
		return this.toRm;
	}

	public void setToRm(Double toRm) 
	{
		this.toRm = toRm;
	}
}