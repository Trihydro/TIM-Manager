package com.trihydro.odewrapper.model;

import java.util.List;

public class ControllerResult {

    public List<String> resultMessages;
	public String direction;
	public String clientId;
	public String route;

    public void setResultMessages(List<String> resultMessages){
		this.resultMessages = resultMessages;
	}

	public List<String> getResultMessages(){
		return this.resultMessages;
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
}