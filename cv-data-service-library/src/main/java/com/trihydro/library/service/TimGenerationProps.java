package com.trihydro.library.service;

import java.math.BigDecimal;

import us.dot.its.jpo.ode.plugin.SituationDataWarehouse.SDW.TimeToLive;

public interface TimGenerationProps {
    public Double getPointIncidentBufferMiles();
    public Double getPathDistanceLimit(); 
    public String[] getRsuRoutes();
	public TimeToLive getSdwTtl();
	public BigDecimal getDefaultLaneWidth();
}
