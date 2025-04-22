package com.trihydro.library.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

public class TimUpdateModel extends ActiveTim {
    // Tim properties
    private int msgCnt;
    private String urlB;
    private Timestamp startDate_Timestamp;
    private Timestamp endDate_Timestamp;
    private String packetId;

    // Tim Type properties
    private String timTypeName;
    private String timTypeDescription;

    // Region properties
    private Integer regionId;
    private String regionDescription;
    private BigDecimal laneWidth;
    private BigDecimal anchorLat;
    private BigDecimal anchorLong;
    private String regionDirection;
    private String directionality;
    private Boolean closedPath;
    private Integer pathId;

    // DataFrame properties
    private int dataFrameId;
    private TravelerInfoType frameType;
    private int durationTime;
    private short doNotUse2;
    private short doNotUse1;
    private short doNotUse4;
    private short doNotUse3;
    private ContentEnum dfContent;
    private String url;

    public BigDecimal getLaneWidth() {
        return laneWidth;
        // DataFrame df;df.setFrameType(frameType);
    }

    public String getRegionDirection() {
        return regionDirection;
    }

    public void setRegionDirection(String regionDirection) {
        this.regionDirection = regionDirection;
    }

    public String getRegionDescription() {
        return regionDescription;
    }

    public void setRegionDescription(String regionDescription) {
        this.regionDescription = regionDescription;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public String getTimTypeName() {
        return timTypeName;
    }

    public void setTimTypeName(String timTypeName) {
        this.timTypeName = timTypeName;
    }

    public String getTimTypeDescription() {
        return timTypeDescription;
    }

    public void setTimTypeDescription(String timTypeDescription) {
        this.timTypeDescription = timTypeDescription;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

    public Timestamp getEndDate_Timestamp() {
        return endDate_Timestamp;
    }

    public void setEndDate_Timestamp(Timestamp endDate_Timestamp) {
        this.endDate_Timestamp = endDate_Timestamp;
    }

    public Timestamp getStartDate_Timestamp() {
        return startDate_Timestamp;
    }

    public void setStartDate_Timestamp(Timestamp startDate_Timestamp) {
        this.startDate_Timestamp = startDate_Timestamp;
    }

    public String getUrlB() {
        return urlB;
    }

    public void setUrlB(String urlB) {
        this.urlB = urlB;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPathId() {
        return pathId;
    }

    public void setPathId(Integer pathId) {
        this.pathId = pathId;
    }

    public Boolean getClosedPath() {
        return closedPath;
    }

    public void setClosedPath(Boolean closedPath) {
        this.closedPath = closedPath;
    }

    public String getDirectionality() {
        return directionality;
    }

    public void setDirectionality(String directionality) {
        this.directionality = directionality;
    }

    public int getDataFrameId() {
        return dataFrameId;
    }

    public void setDataFrameId(int dataFrameId) {
        this.dataFrameId = dataFrameId;
    }

    public TravelerInfoType getFrameType() {
        return frameType;
    }

    public void setFrameType(TravelerInfoType frameType) {
        this.frameType = frameType;
    }

    public short getDoNotUse1() {
        return doNotUse1;
    }

    public void setDoNotUse1(short doNotUse1) {
        this.doNotUse1 = doNotUse1;
    }

    public ContentEnum getDfContent() {
        return dfContent;
    }

    public void setDfContent(ContentEnum dfContent) {
        this.dfContent = dfContent;
    }

    public short getDoNotUse3() {
        return doNotUse3;
    }

    public void setDoNotUse3(short doNotUse3) {
        this.doNotUse3 = doNotUse3;
    }

    public short getDoNotUse4() {
        return doNotUse4;
    }

    public void setDoNotUse4(short doNotUse4) {
        this.doNotUse4 = doNotUse4;
    }

    public short getDoNotUse2() {
        return doNotUse2;
    }

    public void setDoNotUse2(short doNotUse2) {
        this.doNotUse2 = doNotUse2;
    }

    public BigDecimal getAnchorLong() {
        return anchorLong;
    }

    public void setAnchorLong(BigDecimal anchorLong) {
        this.anchorLong = anchorLong;
    }

    public BigDecimal getAnchorLat() {
        return anchorLat;
    }

    public void setAnchorLat(BigDecimal anchorLat) {
        this.anchorLat = anchorLat;
    }

    public int getMsgCnt() {
        return msgCnt;
    }

    public void setMsgCnt(int msgCnt) {
        this.msgCnt = msgCnt;
    }

    public void setLaneWidth(BigDecimal laneWidth) {
        this.laneWidth = laneWidth;
    }
}