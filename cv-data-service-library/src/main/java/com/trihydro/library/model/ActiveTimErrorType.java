package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

/**
 * ActiveTimErrorType based on J2735 DF_TravelerDataFrame content choice (p.105)
 */
public enum ActiveTimErrorType {
    endTime(0), startPoint(1), endPoint(2), itisCodes(3), other(4);

    private int value;
    private static Map<Integer, ActiveTimErrorType> map = new HashMap<Integer, ActiveTimErrorType>();

    private ActiveTimErrorType(int value) {
        this.value = value;
    }

    static {
        for (ActiveTimErrorType ttl : ActiveTimErrorType.values()) {
            map.put(ttl.value, ttl);
        }
    }

    public static ActiveTimErrorType valueOf(int ActiveTimErrorType) {
        return map.get(ActiveTimErrorType);
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        switch (value) {
            case 0:
                return "End Time";

            case 1:
                return "Start Point";

            case 2:
                return "End Point";

            case 3:
                return "ITIS Codes";

            default:
                return "Other";// default to returning other
        }
    }
}