package com.trihydro.library.model;

public class RsuIndexInfo {
    private Integer index;
    private String deliveryStartTime;

    public RsuIndexInfo() {
    }

    public RsuIndexInfo(Integer index, String deliveryStartTime) {
        this.index = index;
        this.deliveryStartTime = deliveryStartTime;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getDeliveryStartTime() {
        return deliveryStartTime;
    }

    public void setDeliveryStartTime(String deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }
}