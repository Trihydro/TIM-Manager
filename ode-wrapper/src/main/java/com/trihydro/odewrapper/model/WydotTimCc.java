package com.trihydro.odewrapper.model;

public class WydotTimCc extends WydotTimBase
{
	private Integer[] advisory;
	private String segment;
	private String district;
	private String id;

	public Integer[] getAdvisory() 
	{
		return this.advisory;
	}

	public void setAdvisory(Integer[] advisory) 
	{
		this.advisory = advisory;
	}

	public String getSegment() 
	{
		return this.segment;
	}

	public void setSegment(String segment) 
	{
		this.segment = segment;
	}

	public String getDistrict() 
	{
		return this.district;
	}

	public void setDistrict(String district) 
	{
		this.district = district;
	}

	public String getId() 
	{
		return this.id;
	}

	public void setId(String id) 
	{
		this.id = id;
	}
}