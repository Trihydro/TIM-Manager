package com.trihydro.tasks.models;

import java.util.List;

import com.trihydro.library.model.AdvisorySituationDataDeposit;

public class CAdvisorySituationDataDeposit implements ISdxComparable {
    private AdvisorySituationDataDeposit asdd;
    private List<Integer> itisCodes;

    public CAdvisorySituationDataDeposit(AdvisorySituationDataDeposit asdd) {
        this.asdd = asdd;
    }

    public AdvisorySituationDataDeposit getAsdd() {
        return asdd;
    }

    public List<Integer> getItisCodes() {
        return itisCodes;
    }

    public void setItisCodes(List<Integer> itisCodes) {
        this.itisCodes = itisCodes;
    }

    public Integer getRecordId() {
        return asdd.getRecordId();
    }
}