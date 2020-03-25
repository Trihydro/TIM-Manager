package com.trihydro.tasks.models;

import java.util.List;

public class Collision {
    private Integer index;
    private List<EnvActiveTim> tims;

    public Collision(Integer index, List<EnvActiveTim> tims) {
        this.index = index;
        this.tims = tims;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public List<EnvActiveTim> getTims() {
        return tims;
    }

    public void setTims(List<EnvActiveTim> tims) {
        this.tims = tims;
    }
}