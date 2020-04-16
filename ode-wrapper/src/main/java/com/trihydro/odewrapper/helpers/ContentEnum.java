package com.trihydro.odewrapper.helpers;

import java.util.HashMap;
import java.util.Map;

/**
 * ContentEnum based on J2735 DF_TravelerDataFrame content choice (p.105)
 */
public enum ContentEnum {
    advisory(0), workZone(1), genericSign(2), speedLimit(3), exitService(4);

    private int value;
    private static Map<Integer, ContentEnum> map = new HashMap<Integer, ContentEnum>();

    private ContentEnum(int value) {
        this.value = value;
    }

    static {
        for (ContentEnum ttl : ContentEnum.values()) {
            map.put(ttl.value, ttl);
        }
    }

    public static ContentEnum valueOf(int contentEnum) {
        return map.get(contentEnum);
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        switch (value) {
            case 0:
                return "advisory";

            case 1:
                return "workZone";

            case 2:
                return "genericSign";

            case 3:
                return "speedLimit";

            case 4:
                return "exitService";

            default:
                return "advisory";// default to returning advisory
        }
    }
}