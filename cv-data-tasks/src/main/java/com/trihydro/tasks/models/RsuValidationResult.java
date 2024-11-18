package com.trihydro.tasks.models;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ActiveTim;

public class RsuValidationResult {
    private boolean rsuUnresponsive;
    private List<Collision> collisions = new ArrayList<>();
    private List<ActiveTimMapping> staleIndexes = new ArrayList<>();
    private List<ActiveTim> missingFromRsu = new ArrayList<>();
    private List<Integer> unaccountedForIndices = new ArrayList<>();

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

    public List<ActiveTimMapping> getStaleIndexes() {
        return staleIndexes;
    }

    public void setStaleIndexes(List<ActiveTimMapping> staleIndexes) {
        this.staleIndexes = staleIndexes;
    }

    public List<ActiveTim> getMissingFromRsu() {
        return missingFromRsu;
    }

    public void setMissingFromRsu(List<ActiveTim> missingFromRsu) {
        this.missingFromRsu = missingFromRsu;
    }

    public List<Integer> getUnaccountedForIndices() {
        return unaccountedForIndices;
    }

    public void setUnaccountedForIndices(List<Integer> unaccountedForIndices) {
        this.unaccountedForIndices = unaccountedForIndices; 
    }
}