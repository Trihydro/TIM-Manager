package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

// Per SEMI_v2.3.0_070616 ASN.1 spec, there's room for additional DialogIDs
public enum DistributionType {
    None(0), // "00000000", not intended for redistribution
    Rsu(1), // "00000001", intended for redistribution over DSRC
    Ip(2); // "00000010" intended for redistribution over IP

    private int value;
    private static Map<Integer, DistributionType> map = new HashMap<Integer, DistributionType>();

    private DistributionType(int value) {
        this.value = value;
    }

    static {
        for (DistributionType dt : DistributionType.values()) {
            map.put(dt.value, dt);
        }
    }

    public static DistributionType valueOf(int distributionType) {
        return map.get(distributionType);
    }

    public int getValue() {
        return value;
    }
}