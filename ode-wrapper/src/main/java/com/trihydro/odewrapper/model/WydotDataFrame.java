package com.trihydro.odewrapper.model;

import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import com.trihydro.library.model.ItisCode;

public class WydotDataFrame extends J2735TravelerInformationMessage.DataFrame
{
	private ItisCode[] itisCodes;	

	public ItisCode[] getItisCodes() {
		return this.itisCodes;
	}             

	public void setItisCodes(ItisCode[] itisCodes) {
		this.itisCodes = itisCodes;
	} 

}
