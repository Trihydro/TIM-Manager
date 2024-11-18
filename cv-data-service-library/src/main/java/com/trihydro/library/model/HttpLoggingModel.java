package com.trihydro.library.model;

import java.sql.Timestamp;

/**
 * HttpLoggingModel
 */
public class HttpLoggingModel {

    private String request;
    private Timestamp requestTime;
    private Timestamp responseTime;

    public String getRequest() {
        return request;
    }

    public Timestamp getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Timestamp responseTime) {
        this.responseTime = responseTime;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}