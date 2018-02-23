package com.trihydro.library.model;

public class DriverAlertType {
    private Integer driverAlertTypeId;
    private String shortName;
    private String description;

    public Integer getDriverAlertTypeId() {
        return this.driverAlertTypeId;
    }

    public void setDriverAlertTypeId(Integer driverAlertTypeId) {
        this.driverAlertTypeId = driverAlertTypeId;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}