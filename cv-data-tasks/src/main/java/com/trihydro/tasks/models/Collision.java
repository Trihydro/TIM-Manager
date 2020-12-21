package com.trihydro.tasks.models;

import java.util.List;

import com.trihydro.library.model.ActiveTim;

public class Collision {
    private Integer index;
    private List<ActiveTim> tims;

    public Collision(Integer index, List<ActiveTim> tims) {
        this.index = index;
        this.tims = tims;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public List<ActiveTim> getTims() {
        return tims;
    }

    public void setTims(List<ActiveTim> tims) {
        this.tims = tims;
    }
}