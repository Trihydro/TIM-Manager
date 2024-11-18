package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

public class TimDeleteSummary {
    private String satelliteErrorSummary;
    private List<Long> successfulSatelliteDeletions;
    private List<Long> successfulRsuDeletions;
    private List<Long> failedActiveTimDeletions;

    private List<String> failedRsuTimJson;

    public TimDeleteSummary() {
        this.successfulSatelliteDeletions = new ArrayList<Long>();
        this.successfulRsuDeletions = new ArrayList<Long>();
        this.failedActiveTimDeletions = new ArrayList<Long>();
        this.failedRsuTimJson = new ArrayList<String>();
    }

    public String getRsuErrorSummary() {
        return String.join(",", failedRsuTimJson);
    }

    public List<String> getFailedRsuTimJson() {
        return failedRsuTimJson;
    }

    public void addfailedRsuTimJson(String json) {
        this.failedRsuTimJson.add(json);
    }

    public void setFailedRsuTimJson(List<String> failedRsuTimJson) {
        this.failedRsuTimJson = failedRsuTimJson;
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