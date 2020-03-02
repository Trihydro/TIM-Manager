package com.trihydro.library.model;

/**
 * Coordinate
 */
public class Coordinate {

    private Double latitude;
    private Double longitude;

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

    public Boolean isValid() {
        return latitude != null && longitude != null;
    }
}