package com.trihydro.library.model;

public class ActiveTimHolding {
    private String direction;
    private String clientId;
    private String satRecordId;
    private String rsuTarget;
    private Coordinate startPoint;
    private Coordinate endPoint;

    public String getDirection() {
        return this.direction;
    }

    public Coordinate getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Coordinate endPoint) {
        this.endPoint = endPoint;
    }

    public Coordinate getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSatRecordId() {
        return this.satRecordId;
    }

    public void setSatRecord(String satRecordId) {
        this.satRecordId = satRecordId;
    }

    public String getRsuTarget() {
        return this.rsuTarget;
    }

    public void setRsuTargetId(String rsuTarget) {
        this.rsuTarget = rsuTarget;
    }

    public ActiveTimHolding() {
    }

    public ActiveTimHolding(WydotTim tim, String rsuTarget, String satRecordId) {
        this.clientId = tim.getClientId();
        this.direction = tim.getDirection();
        this.rsuTarget = rsuTarget;
        this.satRecordId = satRecordId;
        this.startPoint = tim.getStartPoint();
        this.endPoint = tim.getEndPoint();
    }
}