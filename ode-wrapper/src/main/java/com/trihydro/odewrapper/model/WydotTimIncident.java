package com.trihydro.odewrapper.model;

public class WydotTimIncident extends WydotTimBase
{
	private String impact;
    private String problem;
    private String effect;
    private String action;
    private String problemOtherText;
    private String ts;
    private String incidentId;
	private String highway;

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
}