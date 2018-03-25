package com.trihydro.odewrapper.model;

import java.util.List;

import com.trihydro.library.model.Milepost;

import us.dot.its.jpo.ode.model.OdeTravelerInputData;


public class WydotTravelerInputData extends OdeTravelerInputData 
{		
	private String dateSent;
	private String dateReceived;
	private List<Milepost> mileposts;

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

	public List<Milepost> getMileposts() {
		return mileposts;
	}

	public void setMileposts(List<Milepost> mileposts) {
		this.mileposts = mileposts;
	}
}