package com.trihydro.library.model;

import java.util.List;

import us.dot.its.jpo.ode.model.OdeTravelerInputData;

public class WydotTravelerInputData extends OdeTravelerInputData {
    private static final long serialVersionUID = 4901957472231480432L;
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