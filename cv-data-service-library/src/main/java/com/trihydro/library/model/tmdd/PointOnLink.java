package com.trihydro.library.model.tmdd;

public class PointOnLink {
    private GeoLocation geoLocation;
    private String pointName;
    private String linearReference;

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getLinearReference() {
        return linearReference;
    }

    public void setLinearReference(String linearReference) {
        this.linearReference = linearReference;
    }
}
