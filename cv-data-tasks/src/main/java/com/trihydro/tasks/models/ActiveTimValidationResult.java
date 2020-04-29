package com.trihydro.tasks.models;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ActiveTim;

public class ActiveTimValidationResult {
    private ActiveTim activeTim;
    private List<ActiveTimError> errors = new ArrayList<>();

    public ActiveTim getActiveTim() {
        return activeTim;
    }

    public void setActiveTim(ActiveTim activeTim) {
        this.activeTim = activeTim;
    }

    public List<ActiveTimError> getErrors() {
        return errors;
    }

    public void setErrors(List<ActiveTimError> errors) {
        this.errors = errors;
    }
}