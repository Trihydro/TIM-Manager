package com.trihydro.library.model;

public class ResubmitTimException {
    private Long activeTimId;
    private String exceptionMessage;

    public ResubmitTimException() {
    }

    public ResubmitTimException(Long id, String message) {
        this.activeTimId = id;
        this.exceptionMessage = message;
    }

    public Long getActiveTimId() {
        return activeTimId;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public void setActiveTimId(Long activeTimId) {
        this.activeTimId = activeTimId;
    }
}
