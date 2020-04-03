package com.trihydro.tasks.models;

import com.trihydro.library.model.RsuIndexInfo;

public class ActiveTimMapping {
    private EnvActiveTim envTim;
    private RsuIndexInfo rsuIndexInfo;

    public ActiveTimMapping(EnvActiveTim envTim, RsuIndexInfo rsuIndexInfo) {
        this.envTim = envTim;
        this.rsuIndexInfo = rsuIndexInfo;
    }

    public EnvActiveTim getEnvTim() {
        return envTim;
    }

    public void setEnvTim(EnvActiveTim envTim) {
        this.envTim = envTim;
    }

    public RsuIndexInfo getRsuIndexInfo() {
        return rsuIndexInfo;
    }

    public void setRsuIndexInfo(RsuIndexInfo rsuIndexInfo) {
        this.rsuIndexInfo = rsuIndexInfo;
    }
}