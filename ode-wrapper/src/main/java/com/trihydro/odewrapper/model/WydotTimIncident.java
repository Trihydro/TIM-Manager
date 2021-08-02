package com.trihydro.odewrapper.model;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotTim;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimIncident extends WydotTim {

    @ApiModelProperty(value = "Expected values are mudslide, crashes, crash, hazMat, trainDerail, livestock, local, stall, stallSemi, slow, slowOver, stop, flood, avalanche, avalancheControl, landslide, rockslide, wildfire, downPowerline, signInstall, roadDamage, pilotCar, maintenance, mowing, cops, emerVeh")
    private String problem;

    @ApiModelProperty(value = "Expected values are leftClosed, centerClosed, rightClosed, allClosed, shoulderClosed, travelBlocked")
    private String effect;

    @ApiModelProperty(value = "Expected values are caution, slow, delays, stop, toRamp, toShoulder, toHighway, useAlt")
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