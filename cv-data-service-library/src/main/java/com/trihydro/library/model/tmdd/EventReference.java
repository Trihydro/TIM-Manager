package com.trihydro.library.model.tmdd;

public class EventReference {
    private String eventId;
    private Integer eventUpdate;
    private DateTimeZone updateTime;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Integer getEventUpdate() {
        return eventUpdate;
    }

    public void setEventUpdate(Integer eventUpdate) {
        this.eventUpdate = eventUpdate;
    }

    public DateTimeZone getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(DateTimeZone updateTime) {
        this.updateTime = updateTime;
    }
}
