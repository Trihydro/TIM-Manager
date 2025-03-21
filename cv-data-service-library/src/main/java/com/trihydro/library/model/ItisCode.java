package com.trihydro.library.model;

public class ItisCode
{
	private Integer itisCodeId;
	private Integer itisCode;
	private String description;
	private Integer categoryId;

	public ItisCode() {

	}

	public ItisCode(Integer itisCodeId, Integer itisCode, String description, Integer categoryId) {
		this.itisCodeId = itisCodeId;
		this.itisCode = itisCode;
		this.description = description;
		this.categoryId = categoryId;
	}

	public Integer getItisCodeId() 
	{
		return this.itisCodeId;
	}

	public void setItisCodeId(Integer itisCodeId) 
	{
		this.itisCodeId = itisCodeId;
	}

	public Integer getItisCode() 
	{
		return this.itisCode;
	}

	public void setItisCode(Integer itisCode) 
	{
		this.itisCode = itisCode;
	}

	public String getDescription() 
	{
		return this.description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}

	public Integer getCategoryId() 
	{
		return this.categoryId;
	}
	public void setCategoryId(Integer categoryId) 
	{
		this.categoryId = categoryId;
	}
}