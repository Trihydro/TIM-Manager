package com.trihydro.tasks.models;

import java.util.List;

import com.trihydro.library.model.AdvisorySituationDataDeposit;

public class CAdvisorySituationDataDeposit extends SdxComparable {
    private AdvisorySituationDataDeposit asdd;
    private List<Integer> itisCodes;

    public CAdvisorySituationDataDeposit(AdvisorySituationDataDeposit asdd, List<Integer> itisCodes) {
        this.asdd = asdd;
        this.itisCodes = itisCodes;
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