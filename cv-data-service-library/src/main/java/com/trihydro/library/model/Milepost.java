package com.trihydro.library.model;

public class Milepost {
    private String commonName;
    private Double milepost;
    private String direction;
    private Double latitude;
    private Double longitude;
    private Double bearing;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String route) {
        this.commonName = route;
    }

    public Double getMilepost() {
        return milepost;
    }

    public void setMilepost(Double milepost) {
        this.milepost = milepost;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // TODO: this is used to determine tim direction, but is no longer in the view
    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }
}