package com.trihydro.odewrapper.model;

import com.trihydro.library.model.WydotTim;

public class WydotTimVsl extends WydotTim {

    private Integer speed;

    private String deviceId;

    private boolean isOffline;

    public WydotTimVsl() {

    }

    public boolean getIsOffline() {
        return isOffline;
    }

    public void setIsOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public WydotTimVsl(WydotTimVsl o) {
        super(o);
        this.speed = o.speed;
        this.deviceId = o.deviceId;
    }

    @Override
    public WydotTimVsl copy() {
        return new WydotTimVsl(this);
    }

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