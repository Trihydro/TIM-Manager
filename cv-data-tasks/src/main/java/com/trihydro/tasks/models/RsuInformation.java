package com.trihydro.tasks.models;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.WydotRsu;

public class RsuInformation {
    private String ipv4Address;
    private List<ActiveTim> rsuActiveTims = new ArrayList<>();

    public RsuInformation(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public RsuInformation(WydotRsu rsu) {
        this.ipv4Address = rsu.getRsuTarget();
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

    // If both objects have the same ipv4 address, they're targeting the
    // same RSU and should be considered equal.
    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        
        if(!(obj instanceof RsuInformation))
            return false;

        var toCompare = (RsuInformation)obj;

        if(ipv4Address == null)
        {
            // If both don't have an ipv4 address, consider them equal.
            if(toCompare.ipv4Address == null)
                return true;
            else
                return false;
        }

        return ipv4Address.equals(toCompare.ipv4Address);
    }
}