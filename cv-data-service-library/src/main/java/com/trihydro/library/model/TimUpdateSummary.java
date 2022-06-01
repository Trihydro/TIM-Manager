package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

public class TimUpdateSummary {
    private List<Long> successfulTimUpdates;
    private List<Long> failedActiveTimUpdates;

    public TimUpdateSummary() {
        this.successfulTimUpdates = new ArrayList<Long>();
        this.failedActiveTimUpdates = new ArrayList<Long>();
    }

    public List<Long> getFailedActiveTimUpdates() {
        return failedActiveTimUpdates;
    }

    public void setFailedActiveTimUpdates(List<Long> failedActiveTimUpdates) {
        this.failedActiveTimUpdates = failedActiveTimUpdates;
    }

    public void addFailedActiveTimUpdates(Long aTimUpdateFail) {
        this.failedActiveTimUpdates.add(aTimUpdateFail);
    }

    public List<Long> getSuccessfulTimUpdates() {
        return successfulTimUpdates;
    }

    public void setSuccessfulTimUpdates(List<Long> rsuUpdateSuccess) {
        this.successfulTimUpdates = rsuUpdateSuccess;
    }

    public void addSuccessfulTimUpdates(Long rsuUpdateSuccess) {
        this.successfulTimUpdates.add(rsuUpdateSuccess);
    }
}
