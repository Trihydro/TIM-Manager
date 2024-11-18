package com.trihydro.library.model;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ActiveTim {

    private Long activeTimId;
    private Long timId;
    private String timType;
    private Long timTypeId;
    private String direction;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp startTimestamp;
    private String startDateTime;
    private String endDateTime;
    private String expirationDateTime;
    private String route;
    private String clientId;
    private String satRecordId;
    private Integer pk;
    private String rsuTarget;
    private Integer rsuIndex;
    private List<Integer> itisCodes;
    private Coordinate startPoint;
    private Coordinate endPoint;
    private Integer projectKey;

    public Coordinate getEndPoint() {
        return endPoint;
    }

    public String getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(String expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
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

    public Long getActiveTimId() {
        return this.activeTimId;
    }

    public void setActiveTimId(Long activeTimId) {
        this.activeTimId = activeTimId;
    }

    public Long getTimId() {
        return this.timId;
    }

    public void setTimId(Long timId) {
        this.timId = timId;
    }

    public String getTimType() {
        return this.timType;
    }

    public void setTimType(String timType) {
        this.timType = timType;
    }

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getStartDateTime() {
        return this.startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Timestamp getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getEndDateTime() {
        return this.endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Long getTimTypeId() {
        return this.timTypeId;
    }

    public void setTimTypeId(Long timTypeId) {
        this.timTypeId = timTypeId;
    }

    public String getRoute() {
        return this.route;
    }

    public void setRoute(String route) {
        this.route = route;
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

    public Integer getPk() {
        return this.pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public String getRsuTarget() {
        return this.rsuTarget;
    }

    public void setRsuTarget(String rsuTarget) {
        this.rsuTarget = rsuTarget;
    }

    public Integer getRsuIndex() {
        return this.rsuIndex;
    }

    public void setRsuIndex(Integer rsuIndex) {
        this.rsuIndex = rsuIndex;
    }

    public List<Integer> getItisCodes() {
        return itisCodes;
    }

    public void setItisCodes(List<Integer> itisCodes) {
        this.itisCodes = itisCodes;
    }

    public Integer getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(Integer projectKey) {
        this.projectKey = projectKey;
    }

    public boolean isIdenticalConditions(List<Integer> itisCodes, String endDateTime) {
        // check if existing condition is identical to requested condition
        boolean identicalITISCodes = false;
        boolean identicalEndDate = false;
        List<Integer> existingITISCodes = getItisCodes();
        if (existingITISCodes != null) {
            if (existingITISCodes.equals(itisCodes)) {
                identicalITISCodes = true;
            }
        }

        // check if end_date is identical
        if (getEndDateTime() != null) {
            // existing condition has an end date, check if it is identical
            if (getEndDateTime().equals(endDateTime)) {
                identicalEndDate = true;
            }
        } else {
            // existing condition has no end date, check if requested condition has no end
            // date
            if (endDateTime == null) {
                identicalEndDate = true;
            }
        }
        return identicalITISCodes && identicalEndDate;
    }
}