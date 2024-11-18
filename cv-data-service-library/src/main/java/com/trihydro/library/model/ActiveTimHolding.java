package com.trihydro.library.model;

import java.time.Instant;

public class ActiveTimHolding {
    private Long activeTimHoldingId;
    private String direction;
    private String clientId;
    private String satRecordId;
    private String rsuTarget;
    private Integer rsuIndex;
    private Coordinate startPoint;
    private Coordinate endPoint;
    private String dateCreated;
    private Integer projectKey;
    private String expirationDateTime;
    private String packetId;


    public ActiveTimHolding(WydotTim tim, String rsuTarget, String satRecordId, Coordinate endPt) {
        this.clientId = tim.getClientId();
        this.direction = tim.getDirection();
        this.rsuTarget = rsuTarget;
        this.satRecordId = satRecordId;
        this.startPoint = tim.getStartPoint();
        this.endPoint = endPt;
        this.dateCreated = Instant.now().toString();
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public String getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(String expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getRsuIndex() {
        return rsuIndex;
    }

    public void setRsuIndex(Integer rsuIndex) {
        this.rsuIndex = rsuIndex;
    }

    public Long getActiveTimHoldingId() {
        return activeTimHoldingId;
    }

    public void setActiveTimHoldingId(Long activeTimHoldingId) {
        this.activeTimHoldingId = activeTimHoldingId;
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

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSatRecordId() {
        return this.satRecordId;
    }

    public void setSatRecordId(String satRecordId) {
        this.satRecordId = satRecordId;
    }

    public String getRsuTarget() {
        return this.rsuTarget;
    }

    public void setRsuTargetId(String rsuTarget) {
        this.rsuTarget = rsuTarget;
    }

    public ActiveTimHolding() {
        this.dateCreated = Instant.now().toString();
    }

    public Integer getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(Integer projectKey) {
        this.projectKey = projectKey;
    }
}