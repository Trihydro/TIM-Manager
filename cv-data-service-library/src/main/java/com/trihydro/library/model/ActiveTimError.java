package com.trihydro.library.model;

public class ActiveTimError {
    private ActiveTimErrorType name;
    private String timValue;
    private String tmddValue;

    public ActiveTimError(ActiveTimErrorType name, String timValue, String tmddValue) {
        this.name = name;
        this.timValue = timValue;
        this.tmddValue = tmddValue;
    }

    public ActiveTimErrorType getName() {
        return name;
    }

    public void setName(ActiveTimErrorType name) {
        this.name = name;
    }

    public String getTimValue() {
        return timValue;
    }

    public void setTimValue(String timValue) {
        this.timValue = timValue;
    }

    public String getTmddValue() {
        return tmddValue;
    }

    public void setTmddValue(String tmddValue) {
        this.tmddValue = tmddValue;
    }
}