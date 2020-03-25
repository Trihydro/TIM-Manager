package com.trihydro.tasks.models;

import java.util.ArrayList;
import java.util.List;

public class PopulatedRsu {
    private String ipv4Address;
    private List<EnvActiveTim> rsuActiveTims = new ArrayList<>();

    public PopulatedRsu(String ipv4Address) {
        this.ipv4Address = ipv4Address;
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