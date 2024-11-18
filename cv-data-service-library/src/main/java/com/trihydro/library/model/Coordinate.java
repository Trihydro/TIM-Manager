package com.trihydro.library.model;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;

/**
 * Coordinate
 */
public class Coordinate {

    @ApiModelProperty(required = true)
    private BigDecimal latitude;
    @ApiModelProperty(required = true)
    private BigDecimal longitude;

    public Coordinate() {
    }

    public Coordinate(BigDecimal lat, BigDecimal lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    @ApiModelProperty(hidden = true)
    public Boolean isValid() {
        return latitude != null && longitude != null;
    }
}