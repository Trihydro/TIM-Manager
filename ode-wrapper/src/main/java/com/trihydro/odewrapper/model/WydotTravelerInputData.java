package com.trihydro.odewrapper.model;

import us.dot.its.jpo.ode.model.OdeTravelerInputData;


public class WydotTravelerInputData extends OdeTravelerInputData 
{		
	private String dateSent;
	private String dateReceived;

	public String getDateSent() {
		return dateSent;
	}

	public void setDateSent(String dateSent) {
		this.dateSent = dateSent;
	}

	public String getDateReceived() {
		return dateReceived;
	}

	public void setDateReceived(String dateReceived) {
		this.dateReceived = dateReceived;
	}	
}