package com.trihydro.library.model.tmdd;

import java.util.List;

public class FullEventUpdate {
    private MessageHeader messageHeader;
    private EventReference eventReference;
    private EventHeadline eventHeadline;
    private List<EventElementDetail> eventElementDetails;

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(MessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }

    public EventReference getEventReference() {
        return eventReference;
    }

    public void setEventReference(EventReference eventReference) {
        this.eventReference = eventReference;
    }

    public EventHeadline getEventHeadline() {
        return eventHeadline;
    }

    public void setEventHeadline(EventHeadline eventHeadline) {
        this.eventHeadline = eventHeadline;
    }

    public List<EventElementDetail> getEventElementDetails() {
        return eventElementDetails;
    }

    public void setEventElementDetails(List<EventElementDetail> eventElementDetails) {
        this.eventElementDetails = eventElementDetails;
    }
}