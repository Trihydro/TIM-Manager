package com.trihydro.odewrapper.model;

import java.util.List;

public class ControllerResult {

	public List<String> resultMessages;
	public List<String> itisCodes;
	public String direction;
	public String clientId;
	public String route;
	public Double fromRm;
	public Double toRm;

    public void setResultMessages(List<String> resultMessages){
		this.resultMessages = resultMessages;
	}

	public List<String> getResultMessages(){
		return this.resultMessages;
	}
	
	public void setItisCodes(List<String> itisCodes){
		this.itisCodes = itisCodes;
	}

	public List<String> getItisCodes(){
		return this.itisCodes;
    }
    
    public void setDirection(String direction){
		this.direction = direction;
	}

	public String getDirection(){
		return this.direction;
	}

	public void setClientId(String clientId){
		this.clientId = clientId;
	}

	public String getClientId(){
		return this.clientId;
	}

	public void setRoute(String route){
		this.route = route;
	}

	public String getRoute(){
		return this.route;
	}

	public void setFromRm(Double fromRm){
		this.fromRm = fromRm;
	}

	public Double getFromRm(){
		return this.fromRm;
	}

	public void setToRm(Double toRm){
		this.toRm = toRm;
	}

	public Double getToRm(){
		return this.toRm;
	}
}