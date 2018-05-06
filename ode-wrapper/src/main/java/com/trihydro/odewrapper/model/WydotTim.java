package com.trihydro.odewrapper.model;

public class WydotTim
{
	private String direction;
	private Double fromRm;
	private Double toRm;	
	private String roadCode;
	private String route;
	private Integer speed;
	private Integer[] advisory;
	private String startDateTime;
	private String endDateTime;
	private String clientId;	
	private String segment;
	private String district;
	private String id;
	private String impact;
    private String problem;
    private String effect;
    private String action;
    private String problemOtherText;
    private String ts;
    private String incidentId;
	private String highway;
	private Integer pk;
	private String resultMessage;
	private String availability;
	private Double mileMarker;

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

	public String getClientId()
	{
		return this.clientId;
	}

	public void setClientId(String clientId) 
	{
		this.clientId = clientId;
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

	public Integer getSpeed() 
	{
		return this.speed;
	}

	public void setSpeed(Integer speed) 
	{
		this.speed = speed;
	}

	public String getImpact() 
	{
		return this.impact;
	}

	public void setImpact(String impact) 
	{
		this.impact = impact;
	}

	public String getProblem() 
	{
		return this.problem;
	}

	public void setProblem(String problem) 
	{
		this.problem = problem;
    }
    
    public String getEffect() 
	{
		return this.effect;
	}

	public void setEffect(String effect) 
	{
		this.effect = effect;
    }
    
    public String getAction() 
	{
		return this.action;
	}

	public void setAction(String action) 
	{
		this.action = action;
    }
    
    public String getProblemOtherText() 
	{
		return this.problemOtherText;
	}

    public void setProblemOtherText(String problemOtherText) 
	{
		this.problemOtherText = problemOtherText;
    }
    
    public String getTs() 
	{
		return this.ts;
	}

    public void setTs(String ts) 
	{
		this.ts = ts;
    }
    
    public String getIncidentId() 
	{
		return this.incidentId;
	}

    public void setIncidentId(String incidentId) 
	{
		this.incidentId = incidentId;
	}

	public String getHighway() 
	{
		return this.highway;
	}

    public void setHighway(String highway) 
	{
		this.highway = highway;
	}

	public Integer getPk() 
	{
		return this.pk;
	}

    public void setPk(Integer pk) 
	{
		this.pk = pk;
	}

	public String getResultMessage() 
	{
		return this.resultMessage;
	}

    public void setResultMessage(String resultMessage) 
	{
		this.resultMessage = resultMessage;
	}

	public Double getMileMarker() 
	{
		return this.mileMarker;
	}

    public void setMileMarker(Double mileMarker) 
	{
		this.mileMarker = mileMarker;
	}

	public String getAvailability() 
	{
		return this.availability;
	}

    public void setAvailability(String availability) 
	{
		this.availability = availability;
	}
}