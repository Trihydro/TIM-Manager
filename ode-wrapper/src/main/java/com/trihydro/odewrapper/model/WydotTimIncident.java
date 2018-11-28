package com.trihydro.odewrapper.model;

public class WydotTimIncident extends WydotTim {

    private String impact;
    private String problem;
    private String effect;
    private String action;
    private String incidentId;
    private String highway;
    private String schedStart;
    private String schedEnd;
    private Integer pk;
    private String problemOtherText;

    public String getSchedStart() {
        return this.schedStart;
    }

    public void setSchedStart(String schedStart) {
        this.schedStart = schedStart;
    }

    public String getSchedEnd() {
        return this.schedEnd;
    }

    public void setSchedEnd(String schedEnd) {
        this.schedEnd = schedEnd;
    }

    public String getProblemOtherText() {
        return this.problemOtherText;
    }

    public void setProblemOtherText(String problemOtherText) {
        this.problemOtherText = problemOtherText;
    }

    public Integer getPk() {
        return this.pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public String getHighway() {
        return this.highway;
    }

    public void setHighway(String highway) {
        this.highway = highway;
    }

    public String getIncidentId() {
        return this.incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getProblem() {
        return this.problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getEffect() {
        return this.effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getImpact() {
        return this.impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }
    
}