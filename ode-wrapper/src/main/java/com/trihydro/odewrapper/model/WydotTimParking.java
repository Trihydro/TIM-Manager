package com.trihydro.odewrapper.model;

public class WydotTimParking extends WydotTim {

    private Double mileMarker;
    private Integer availability;
    private String exit;

    public String getExit() {
        return this.exit;
    }

    public void setExit(String exit) {
        this.exit = exit;
    }

    public Integer getAvailability() {
        return this.availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    public Double getMileMarker() {
        return this.mileMarker;
    }

    public void setMileMarker(Double mileMarker) {
        this.mileMarker = mileMarker;
    }

}