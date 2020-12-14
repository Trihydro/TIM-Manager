package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

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