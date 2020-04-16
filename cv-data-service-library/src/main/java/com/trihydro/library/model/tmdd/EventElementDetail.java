package com.trihydro.library.model.tmdd;

import java.util.List;

public class EventElementDetail {
    private EventTimes eventTimes;
    private List<EventDescription> eventDescriptions;
    private List<EventLocation> eventLocations;

    public EventTimes getEventTimes() {
        return eventTimes;
    }

    public void setEventTimes(EventTimes eventTimes) {
        this.eventTimes = eventTimes;
    }

    public List<EventDescription> getEventDescriptions() {
        return eventDescriptions;
    }

    public void setEventDescriptions(List<EventDescription> eventDescriptions) {
        this.eventDescriptions = eventDescriptions;
    }

    public List<EventLocation> getEventLocations() {
        return eventLocations;
    }

    public void setEventLocations(List<EventLocation> eventLocations) {
        this.eventLocations = eventLocations;
    }
}