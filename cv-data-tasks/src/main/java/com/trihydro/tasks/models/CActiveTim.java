package com.trihydro.tasks.models;

import java.util.List;

import com.trihydro.library.model.ActiveTim;

public class CActiveTim implements ISdxComparable {
    private ActiveTim activeTim;

    public CActiveTim(ActiveTim activeTim) {
        this.activeTim = activeTim;
    }

    public ActiveTim getActiveTim() {
        return activeTim;
    }

    public List<Integer> getItisCodes() {
        return activeTim.getItisCodes();
    }

    public void setItisCodes(List<Integer> itisCodes) {
        activeTim.setItisCodes(itisCodes);
    }

    public Integer getRecordId() {
        Integer recordId = null;

        try {
            recordId = Integer.parseUnsignedInt(activeTim.getSatRecordId(), 16);
        } catch (NumberFormatException ex) {
            // Unable to parse, recordId should remain null
        }

        return recordId;
    }
}