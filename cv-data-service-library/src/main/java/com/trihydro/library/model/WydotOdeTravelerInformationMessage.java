package com.trihydro.library.model;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

public class WydotOdeTravelerInformationMessage extends OdeTravelerInformationMessage {

    private static final long serialVersionUID = 1L;
    private Integer rsuIndex;

    public Integer getRsuIndex() {
        return rsuIndex;
    }

    public void setRsuIndex(Integer rsuIndex) {
        this.rsuIndex = rsuIndex;
    }
}
