package com.trihydro.odewrapper.model;

import com.trihydro.library.model.ItisCode;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

public class WydotDataFrame extends OdeTravelerInformationMessage.DataFrame {
	private static final long serialVersionUID = -207518977919223715L;
	private ItisCode[] itisCodes;

	public ItisCode[] getItisCodes() {
		return this.itisCodes;
	}

	public void setItisCodes(ItisCode[] itisCodes) {
		this.itisCodes = itisCodes;
	}

}
