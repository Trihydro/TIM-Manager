package com.trihydro.library.model;

public class TimType {
    private Long timTypeId;
    private String type;
    private String description;

    public Long getTimTypeId() {
        return this.timTypeId;
    }

    public void setTimTypeId(Long timTypeId) {
        this.timTypeId = timTypeId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}