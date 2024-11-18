package com.trihydro.library.model;

import java.util.List;

public class TimQuery {

    public List<Integer> indicies_set;

    public void setIndicies_set(List<Integer> indicies_set) {
        this.indicies_set = indicies_set;
    }

    public List<Integer> getIndicies_set() {
        return this.indicies_set;
    }

    public void appendIndex(Integer index) {
        if (index != null && !indicies_set.contains(index)) {
            indicies_set.add(index);
        }
    }
}