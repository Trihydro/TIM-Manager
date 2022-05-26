package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

public class TimUpdateSummary {
    private List<Long> successfulSatelliteUpdates;
    private List<Long> successfulRsuUpdates;
    private List<Long> failedActiveTimDeletions;

    public TimUpdateSummary() {
        this.successfulSatelliteUpdates = new ArrayList<Long>();
        this.successfulRsuUpdates = new ArrayList<Long>();
        this.failedActiveTimDeletions = new ArrayList<Long>();
    }

    public List<Long> getFailedActiveTimDeletions() {
        return failedActiveTimDeletions;
    }

    public void setFailedActiveTimDeletions(List<Long> failedActiveTimDeletions) {
        this.failedActiveTimDeletions = failedActiveTimDeletions;
    }

    public void addFailedActiveTimDeletions(Long aTimDelFail) {
        this.failedActiveTimDeletions.add(aTimDelFail);
    }

    public List<Long> getSuccessfulRsuUpdataes() {
        return successfulRsuUpdates;
    }

    public void setSuccessfulRsuUpdates(List<Long> rsuDelSuccess) {
        this.successfulRsuUpdates = rsuDelSuccess;
    }

    public void addSuccessfulRsuUpdates(Long rsuDelSuccess) {
        this.successfulRsuUpdates.add(rsuDelSuccess);
    }

    public List<Long> getSuccessfulSatelliteUpdates() {
        return successfulSatelliteUpdates;
    }

    public void setSuccessfulSatelliteUpdates(List<Long> satDelSuccess) {
        this.successfulSatelliteUpdates = satDelSuccess;
    }
}
