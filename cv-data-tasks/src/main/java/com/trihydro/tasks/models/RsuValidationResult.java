package com.trihydro.tasks.models;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ActiveTim;

public class RsuValidationResult {
    private String rsuAddress;
    private boolean rsuUnresponsive;
    private List<Collision> collisions = new ArrayList<>();
    private List<EnvActiveTim> missingFromRsu = new ArrayList<>();
    private List<Integer> unaccountedForIndices = new ArrayList<>();

    public RsuValidationResult(String rsuAddress) {
        this.rsuAddress = rsuAddress;
    }

    public String getRsu() {
        return rsuAddress;
    }

    public boolean getRsuUnresponsive() {
        return rsuUnresponsive;
    }

    public void setRsuUnresponsive(boolean rsuUnresponsive) {
        this.rsuUnresponsive = rsuUnresponsive;
    }

    public List<Collision> getCollisions() {
        return collisions;
    }

    public void setCollisions(List<Collision> collisions) {
        this.collisions = collisions;
    }

    public List<EnvActiveTim> getMissingFromRsu() {
        return missingFromRsu;
    }

    public void setMissingFromRsu(List<EnvActiveTim> missingFromRsu) {
        this.missingFromRsu = missingFromRsu;
    }

    public List<Integer> getUnaccountedForIndices() {
        return unaccountedForIndices;
    }

    public void setUnaccountedForIndices(List<Integer> unaccountedForIndices) {
        this.unaccountedForIndices = unaccountedForIndices; 
    }
}