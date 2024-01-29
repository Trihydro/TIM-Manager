package com.trihydro.odewrapper.model;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotTim;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimIncident extends WydotTim {

    @ApiModelProperty(value = "Expected values are mudslide, livestock, avalanche, avalancheControl, landslide, wildfire, signInstall, mowing")
    private String problem;
    private String effect;

    @ApiModelProperty(value = "Expected values are caution, delays, stop")
    private String action;
    @ApiModelProperty(required = true)
    private String incidentId;
    @ApiModelProperty(required = true)
    private String highway;

    private Integer pk;
    @ApiModelProperty(hidden = true)
    private transient String clientId;
    @ApiModelProperty(hidden = true)
    private transient String route;
    @ApiModelProperty(hidden = true)
    private transient List<String> itisCodes;

    @ApiModelProperty(value = "Optional. If not provided, a TIM will be generated extending 1 mile upstream from the startPoint", required = false)
    private transient Coordinate endPoint;

    public WydotTimIncident() {

    }

    public WydotTimIncident(WydotTimIncident o) {
        super(o);
        this.problem = o.problem;
        this.effect = o.effect;
        this.action = o.action;
        this.incidentId = o.incidentId;
        this.highway = o.highway;
        this.pk = o.pk;
        this.clientId = o.clientId;
        this.route = o.route;

        if (o.itisCodes != null)
            this.itisCodes = new ArrayList<>(o.itisCodes);
        if (o.endPoint != null)
            this.endPoint = new Coordinate(o.endPoint.getLatitude(), o.endPoint.getLongitude());
    }

    @Override
    public WydotTimIncident copy() {
        return new WydotTimIncident(this);
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
}