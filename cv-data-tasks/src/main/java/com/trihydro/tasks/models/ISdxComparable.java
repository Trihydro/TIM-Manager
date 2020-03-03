package com.trihydro.tasks.models;

import java.util.List;

public interface ISdxComparable {
    public Integer getRecordId();
    public List<Integer> getItisCodes();

    public void setItisCodes(List<Integer> itisCodes);
}