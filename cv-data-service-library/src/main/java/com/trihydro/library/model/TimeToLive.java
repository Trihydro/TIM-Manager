package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

// Per SEMI_v2.3.0_070616 ASN.1 spec.
public enum TimeToLive {
    Minute(0), HalfHour(1), Day(2), Week(3), Month(4), Year(5);

    private int value;
    private static Map<Integer, TimeToLive> map = new HashMap<Integer, TimeToLive>();

    private TimeToLive(int value) {
        this.value = value;
    }

    static {
        for (TimeToLive ttl : TimeToLive.values()) {
            map.put(ttl.value, ttl);
        }
    }

    public static TimeToLive valueOf(int timeToLive) {
        return map.get(timeToLive);
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        switch (value) {
        case 0:
            return "oneminute";

        case 1:
            return "thirtyminutes";

        case 2:
            return "oneday";

        case 3:
            return "oneweek";

        case 4:
            return "onemonth";

        case 5:
            return "oneyear";

        default:
            return "";
        }
    }
}