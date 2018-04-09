package com.trihydro.odewrapper.model;

public class WydotTimRw extends WydotTimBase
{
	private String startDateTime;
	private String endDateTime;
	private Integer[] advisory;
	private String clientId;
	private String route;

	public String getClientId() 
	{
		return this.clientId;
	}

	public void setClientId(String clientId) 
	{
		this.clientId = clientId;
	}

	public String getStartDateTime() 
	{
		return this.startDateTime;
	}

	public void setStartDateTime(String startDateTime) 
	{
		this.startDateTime = startDateTime;
	}

	public String getEndDateTime() 
	{
		return this.endDateTime;
	}

	public void setEndDateTime(String endDateTime) 
	{
		this.endDateTime = endDateTime;
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