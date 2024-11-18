package com.trihydro.library.model;

public class CertExpirationModel {
    private String expirationDate;
    private String packetID;
    private String requiredExpirationDate;
    private String startDateTime;
    
    public String getStartDateTime(){
        return startDateTime;
    }
    
    public void setStartDateTime(String startDateTime){
        this.startDateTime = startDateTime;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getRequiredExpirationDate() {
		return requiredExpirationDate;
	}

	public void setRequiredExpirationDate(String requiredExpirationDate) {
		this.requiredExpirationDate = requiredExpirationDate;
	}

	public String getPacketID() {
        return packetID;
    }

    public void setPacketID(String packetID) {
        this.packetID = packetID;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
