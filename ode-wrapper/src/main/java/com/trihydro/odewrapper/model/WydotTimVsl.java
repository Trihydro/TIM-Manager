package com.trihydro.odewrapper.model;

public class WydotTimVsl extends WydotTimBase
{
	private Integer speed;
	private String route;


	public Integer getSpeed() 
	{
		return this.speed;
	}

	public void setSpeed(Integer speed) 
	{
		this.speed = speed;
	}

	public String getRoute() 
	{
		return this.route;
	}

	public void setRoute(String route) 
	{
		this.route = route;
	}
}