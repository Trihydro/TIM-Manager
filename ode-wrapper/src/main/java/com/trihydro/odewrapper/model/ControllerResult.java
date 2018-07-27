package com.trihydro.odewrapper.model;

public class ControllerResult {

    public int resultCode;
    public String resultMessage;
	public String direction;
	public String clientId;

	public void setResultCode(int resultCode){
		this.resultCode = resultCode;
	}

	public int getResultCode(){
		return this.resultCode;
	}

    public void setResultMessage(String resultMessage){
		this.resultMessage = resultMessage;
	}

	public String getResultMessage(){
		return this.resultMessage;
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
}