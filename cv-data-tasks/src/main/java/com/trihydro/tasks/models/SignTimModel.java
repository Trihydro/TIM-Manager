package com.trihydro.tasks.models;

public class SignTimModel {
    private int sigValidityOverride;
    private String message;

    public int getSigValidityOverride() {
        return sigValidityOverride;
    }

    public void setSigValidity(int sigValidityOverride) {
        this.sigValidityOverride = sigValidityOverride;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
