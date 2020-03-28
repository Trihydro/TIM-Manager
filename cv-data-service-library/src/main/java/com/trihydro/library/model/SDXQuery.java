package com.trihydro.library.model;

public class SDXQuery {
    private String orderByField;
    private String orderByOrder;
    private Integer skip;
    private Integer limit;
    private Integer dialogID;
    private Double nwLat;
    private Double nwLon;
    private Double seLat;
    private Double seLon;
    private String startDate;
    private String startDateOperator;
    private String endDate;
    private String endDateOperator;

    public String getOrderByField() {
        return orderByField;
    }

    public String getOrderByOrder() {
        return orderByOrder;
    }

    public Integer getSkip() {
        return skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getDialogID() {
        return dialogID;
    }

    public Double getNwLat() {
        return nwLat;
    }

    public Double getNwLon() {
        return nwLon;
    }

    public Double getSeLat() {
        return seLat;
    }

    public Double getSeLon() {
        return seLon;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartDateOperator() {
        return startDateOperator;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getEndDateOperator() {
        return endDateOperator;
    }

    public void setOrderByField(String orderByField) {
        this.orderByField = orderByField;
    }

    public void setOrderByOrder(String orderByOrder) {
        this.orderByOrder = orderByOrder;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setDialogID(Integer dialogID) {
        this.dialogID = dialogID;
    }

    public void setNwLat(Double nwLat) {
        this.nwLat = nwLat;
    }

    public void setNwLon(Double nwLon) {
        this.nwLon = nwLon;
    }

    public void setSeLat(Double seLat) {
        this.seLat = seLat;
    }

    public void setSeLon(Double seLon) {
        this.seLon = seLon;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setStartDateOperator(String startDateOperator) {
        this.startDateOperator = startDateOperator;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setEndDateOperator(String endDateOperator) {
        this.endDateOperator = endDateOperator;
    }
}