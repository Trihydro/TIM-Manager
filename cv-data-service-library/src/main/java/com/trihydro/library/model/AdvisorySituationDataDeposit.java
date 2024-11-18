package com.trihydro.library.model;

import java.time.LocalDateTime;

public class AdvisorySituationDataDeposit {
    private String id; // Assigned by Cosmos
    private String encodeType; // From DepositRequest
    private String encodedMsg; // From DepositRequest
    private LocalDateTime createdAt; // Assigned during deposit
    private LocalDateTime expireAt; // Assigned during deposit, based on TimeToLive
    private SemiDialogID dialogId;
    private SemiSequenceID sequenceId;
    private int groupId; // Size 4 OCTET STRING in J2735 ASN.1 Spec
    private int requestId;
    private Integer recordId;
    private TimeToLive timeToLive;
    private double nwLat;
    private double nwLon;
    private double seLat;
    private double seLon;
    private int asdmId;
    private AdvisoryBroadcastType asdmType;
    private DistributionType distType;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private String advisoryMessage;
    private Object region;

    // private Polygon region; // Created during deposit from ServiceRegion (for
    // querying)
    public String getId() {
        return id;
    }

    public Object getRegion() {
        return region;
    }

    public void setRegion(Object region) {
        this.region = region;
    }

    public String getAdvisoryMessage() {
        return advisoryMessage;
    }

    public void setAdvisoryMessage(String advisoryMessage) {
        this.advisoryMessage = advisoryMessage;
    }

    public LocalDateTime getStopTime() {
        return stopTime;
    }

    public void setStopTime(LocalDateTime stopTime) {
        this.stopTime = stopTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public DistributionType getDistType() {
        return distType;
    }

    public void setDistType(DistributionType distType) {
        this.distType = distType;
    }

    public AdvisoryBroadcastType getAsdmType() {
        return asdmType;
    }

    public void setAsdmType(AdvisoryBroadcastType asdmType) {
        this.asdmType = asdmType;
    }

    public int getAsdmId() {
        return asdmId;
    }

    public void setAsdmId(int asdmId) {
        this.asdmId = asdmId;
    }

    public double getSeLon() {
        return seLon;
    }

    public void setSeLon(double seLon) {
        this.seLon = seLon;
    }

    public double getSeLat() {
        return seLat;
    }

    public void setSeLat(double seLat) {
        this.seLat = seLat;
    }

    public double getNwLon() {
        return nwLon;
    }

    public void setNwLon(double nwLon) {
        this.nwLon = nwLon;
    }

    public double getNwLat() {
        return nwLat;
    }

    public void setNwLat(double nwLat) {
        this.nwLat = nwLat;
    }

    public TimeToLive getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(TimeToLive timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public SemiSequenceID getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(SemiSequenceID sequenceId) {
        this.sequenceId = sequenceId;
    }

    public SemiDialogID getDialogId() {
        return dialogId;
    }

    public void setDialogId(SemiDialogID dialogId) {
        this.dialogId = dialogId;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEncodedMsg() {
        return encodedMsg;
    }

    public void setEncodedMsg(String encodedMsg) {
        this.encodedMsg = encodedMsg;
    }

    public String getEncodeType() {
        return encodeType;
    }

    public void setEncodeType(String encodeType) {
        this.encodeType = encodeType;
    }

    public void setId(String id) {
        this.id = id;
    }
}