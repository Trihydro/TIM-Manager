package com.trihydro.tasks.models;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.RsuIndexInfo;

public class ActiveTimMapping {
    private ActiveTim activeTim;
    private RsuIndexInfo rsuIndexInfo;

    public ActiveTimMapping(ActiveTim activeTim, RsuIndexInfo rsuIndexInfo) {
        this.activeTim = activeTim;
        this.rsuIndexInfo = rsuIndexInfo;
    }

    public ActiveTim getActiveTim() {
        return activeTim;
    }

    public void setActiveTim(ActiveTim activeTim) {
        this.activeTim = activeTim;
    }

    public RsuIndexInfo getRsuIndexInfo() {
        return rsuIndexInfo;
    }

    public void setRsuIndexInfo(RsuIndexInfo rsuIndexInfo) {
        this.rsuIndexInfo = rsuIndexInfo;
    }
}