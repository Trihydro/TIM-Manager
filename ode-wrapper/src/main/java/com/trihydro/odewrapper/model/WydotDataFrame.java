package com.trihydro.odewrapper.model;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import com.trihydro.library.model.ItisCode;

public class WydotDataFrame extends OdeTravelerInformationMessage.DataFrame {
	private ItisCode[] itisCodes;

	public ItisCode[] getItisCodes() {
		return this.itisCodes;
	}

	public void setItisCodes(ItisCode[] itisCodes) {
		this.itisCodes = itisCodes;
	}

}
