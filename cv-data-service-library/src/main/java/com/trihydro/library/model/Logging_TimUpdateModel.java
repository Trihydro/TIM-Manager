package com.trihydro.library.model;

public class Logging_TimUpdateModel {
    private Long activeTimId;
    private Long timId;
    private String packetId;

    public Logging_TimUpdateModel() {
    }

    public Logging_TimUpdateModel(TimUpdateModel tum) {
        this.activeTimId = tum.getActiveTimId();
        this.timId = tum.getTimId();
        this.packetId = tum.getPacketId();
    }

    public Long getActiveTimId() {
        return activeTimId;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public Long getTimId() {
        return timId;
    }

    public void setTimId(Long timId) {
        this.timId = timId;
    }

    public void setActiveTimId(Long activeTimId) {
        this.activeTimId = activeTimId;
    }
}
