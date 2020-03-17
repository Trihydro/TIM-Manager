package com.trihydro.library.model;

import java.util.List;

public class PopulatedRsu {
    private Long deviceId;
    private String ipv4Address;
    private List<ActiveTim> rsuActiveTims;

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public List<ActiveTim> getRsuActiveTims() {
        return rsuActiveTims;
    }

    public void setRsuActiveTims(List<ActiveTim> rsuActiveTims) {
        this.rsuActiveTims = rsuActiveTims;
    }
}