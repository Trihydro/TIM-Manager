package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

public class TimDeleteSummary {
    private String satelliteErrorSummary;
    private List<Long> successfulSatelliteDeletions;
    private List<Long> successfulRsuDeletions;

    public TimDeleteSummary() {
        this.successfulSatelliteDeletions = new ArrayList<Long>();
        this.successfulRsuDeletions = new ArrayList<Long>();
    }

    public String getSatelliteErrorSummary() {
        return satelliteErrorSummary;
    }

    public void setSatelliteErrorSummary(String satErrSummary) {
        this.satelliteErrorSummary = satErrSummary;
    }

    public List<Long> getSuccessfulRsuDeletions() {
        return successfulRsuDeletions;
    }

    public void setSuccessfulRsuDeletions(List<Long> rsuDelSuccess) {
        this.successfulRsuDeletions = rsuDelSuccess;
    }

    public void addSuccessfulRsuDeletions(Long rsuDelSuccess) {
        this.successfulRsuDeletions.add(rsuDelSuccess);
    }

    public List<Long> getSuccessfulSatelliteDeletions() {
        return successfulSatelliteDeletions;
    }

    public void setSuccessfulSatelliteDeletions(List<Long> satDelSuccess) {
        this.successfulSatelliteDeletions = satDelSuccess;
    }
}