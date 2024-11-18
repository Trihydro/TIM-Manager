package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

// Per SEMI_v2.3.0_070616 ASN.1 spec.
public enum AdvisoryBroadcastType {
    SpatAggregate(0), Map(1), Tim(2), Ev(3);

    private int value;
    private static Map<Integer,AdvisoryBroadcastType> map = new HashMap<Integer,AdvisoryBroadcastType>();

    private AdvisoryBroadcastType(int value) {
        this.value = value;
    }

    static {
        for (AdvisoryBroadcastType abt : AdvisoryBroadcastType.values()) {
            map.put(abt.value, abt);
        }
    }

    public static AdvisoryBroadcastType valueOf(int advisoryBroadcastType) {
        return map.get(advisoryBroadcastType);
    }

    public int getValue() {
        return value;
    }
}