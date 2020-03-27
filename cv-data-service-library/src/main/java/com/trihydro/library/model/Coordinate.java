package com.trihydro.library.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * Coordinate
 */
public class Coordinate {

    @ApiModelProperty(required = true)
    private Double latitude;
    @ApiModelProperty(required = true)
    private Double longitude;

    public Coordinate() {
    }

    public Coordinate(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @ApiModelProperty(hidden = true)
    public Boolean isValid() {
        return latitude != null && longitude != null;
    }
}