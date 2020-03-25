package com.trihydro.tasks.models;

import java.util.List;

public class PopulatedRsu {
    private Long deviceId;
    private String ipv4Address;
    private List<EnvActiveTim> rsuActiveTims;

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

    public List<EnvActiveTim> getRsuActiveTims() {
        return rsuActiveTims;
    }

    public void setRsuActiveTims(List<EnvActiveTim> rsuActiveTims) {
        this.rsuActiveTims = rsuActiveTims;
    }
}