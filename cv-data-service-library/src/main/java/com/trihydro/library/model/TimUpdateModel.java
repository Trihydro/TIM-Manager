package com.trihydro.library.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TimUpdateModel extends ActiveTim {
    // Tim properties
    private int msgCnt;
    private String urlB;
    private Timestamp startDate_Timestamp;
    private Timestamp endDate_Timestamp;

    // Tim Type properties
    private String timTypeName;
    private String timTypeDescription;

    // Region properties
    private Integer regionId;
    private String regionName;
    private BigDecimal laneWidth;
    private BigDecimal anchorLat;
    private BigDecimal anchorLong;
    private String direction;
    private String directionality;
    private Boolean closedPath;
    private Integer pathId;

    // DataFrame properties
    private int dataFrameId;
    private int frameType;
    private int durationTime;
    private short sspLocationRights;
    private short sspTimRights;
    private short sspMsgTypes;
    private short sspMsgContent;
    private String dfContent;
    private String url;

    public BigDecimal getLaneWidth() {
        return laneWidth;
        // DataFrame df;df.setFrameType(frameType);
    }

    public String getRegionName() {
        return this.regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getDataFrameId() {
        return dataFrameId;
    }

    public void setDataFrameId(int dataFrameId) {
        this.dataFrameId = dataFrameId;
    }

    public int getFrameType() {
        return frameType;
    }

    public void setFrameType(int frameType) {
        this.frameType = frameType;
    }

    public short getSspTimRights() {
        return sspTimRights;
    }

    public void setSspTimRights(short sspTimRights) {
        this.sspTimRights = sspTimRights;
    }

    public String getDfContent() {
        return dfContent;
    }

    public void setDfContent(String dfContent) {
        this.dfContent = dfContent;
    }

    public short getSspMsgContent() {
        return sspMsgContent;
    }

    public void setSspMsgContent(short sspMsgContent) {
        this.sspMsgContent = sspMsgContent;
    }

    public short getSspMsgTypes() {
        return sspMsgTypes;
    }

    public void setSspMsgTypes(short sspMsgTypes) {
        this.sspMsgTypes = sspMsgTypes;
    }

    public short getSspLocationRights() {
        return sspLocationRights;
    }

    public void setSspLocationRights(short sspLocationRights) {
        this.sspLocationRights = sspLocationRights;
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