package com.trihydro.tasks.models;

import java.util.ArrayList;
import java.util.List;

public class RsuValidationRecord {
    private RsuInformation rsuInformation;
    private List<RsuValidationResult> validationResults;
    private String error;

    public RsuValidationRecord(RsuInformation rsuInformation)
    {
        // RSU Information is bound to the validation record,
        // and cannot be changed.
        this.rsuInformation = rsuInformation;
        
        // We should be performing 2 checks on an RSU at most, during any
        // validation period
        validationResults = new ArrayList<RsuValidationResult>(2);
    }

    public RsuInformation getRsuInformation() {
        return rsuInformation;
    }

    public List<RsuValidationResult> getValidationResults() {
        return validationResults;
    }

    public void addValidationResult(RsuValidationResult result) {
        validationResults.add(result);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
}
