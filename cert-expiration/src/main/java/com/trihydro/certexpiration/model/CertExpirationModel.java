package com.trihydro.certexpiration.model;

public class CertExpirationModel {
    private String expirationDate;
    private String packetID;
    private String requireExpirationDate;
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

    public String getRequireExpirationDate() {
		return requireExpirationDate;
	}

	public void setRequireExpirationDate(String requireExpirationDate) {
		this.requireExpirationDate = requireExpirationDate;
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
