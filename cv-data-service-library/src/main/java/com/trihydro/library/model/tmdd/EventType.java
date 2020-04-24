package com.trihydro.library.model.tmdd;

public class EventType {
    private String type;
    private String value;

    public EventType(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String normalized() {
        // "type:valuetext"
        return (type + ":" + value).toLowerCase().replaceAll("\s", "");
    }
}
