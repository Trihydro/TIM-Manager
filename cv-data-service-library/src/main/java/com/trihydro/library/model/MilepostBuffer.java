package com.trihydro.library.model;

public class MilepostBuffer {
    private Coordinate point;
    private String direction;
    private String commonName;
    private Double bufferMiles;

    public Coordinate getPoint() {
        return point;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public Double getBufferMiles() {
        return bufferMiles;
    }

    public void setBufferMiles(Double bufferMiles) {
        this.bufferMiles = bufferMiles;
    }

    public void setPoint(Coordinate point) {
        this.point = point;
    }
}