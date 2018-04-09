package com.trihydro.odewrapper.model;

public class WydotTimRc extends WydotTimBase
{
	private String roadCode;
	private String route;
	private Integer[] advisory;

	public String getRoadCode() 
	{
		return this.roadCode;
	}

	public void setRoadCode(String roadCode) 
	{
		this.roadCode = roadCode;
	}

	public Integer[] getAdvisory() 
	{
		return this.advisory;
	}

	public void setAdvisory(Integer[] advisory) 
	{
		this.advisory = advisory;
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