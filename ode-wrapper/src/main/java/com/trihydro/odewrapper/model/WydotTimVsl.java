package com.trihydro.odewrapper.model;

import com.trihydro.library.model.WydotTim;

public class WydotTimVsl extends WydotTim {

    private Integer speed;

    private String deviceId;

    public Integer getSpeed() {
        return this.speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}