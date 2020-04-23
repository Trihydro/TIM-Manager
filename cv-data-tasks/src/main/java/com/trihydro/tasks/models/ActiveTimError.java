package com.trihydro.tasks.models;

public class ActiveTimError {
    private String name;
    private String timValue;
    private String tmddValue;

    public ActiveTimError(String name, String timValue, String tmddValue) {
        this.name = name;
        this.timValue = timValue;
        this.tmddValue = tmddValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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