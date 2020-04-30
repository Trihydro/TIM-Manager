package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

public enum CustomItisEnum {
    blowOver(7040), closedLHPV1(2689), closedLHPV2(2690), closedLHPV3(2691), closedLHPV4(2692), noLightTrailers(2688),
    closedLocal(904), closedLaw(903), closedMultipleStates(902), closedUtah(901), closedSouthDakota(900),
    closedNebraska(899), closedMontana(898), closedIdaho(897), closedColorado(896);

    private int value;
    private static Map<Integer, CustomItisEnum> map = new HashMap<Integer, CustomItisEnum>();

    private CustomItisEnum(int value) {
        this.value = value;
    }

    static {
        for (CustomItisEnum ttl : CustomItisEnum.values()) {
            map.put(ttl.value, ttl);
        }
    }

    public static CustomItisEnum valueOf(int customItisEnum) {
        return map.get(customItisEnum);
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        switch (value) {
            case 7040:
                return "Extreme blow over risk";

            case 2689:
            case 2690:
            case 2691:
            case 2692:
                return "Closed to light, high profile vehicles";

            case 2688:
                return "Advise no light trailers";

            case 904:
                return "Closed due to local authority request";

            case 903:
                return "Closed due to law enforcement request";

            case 902:
                return "Closed due to border state request from Multiple States";

            case 901:
                return "Closed due to border state request from Utah";

            case 900:
                return "Closed due to border state request from South Dakota";

            case 899:
                return "Closed due to border state request from Nebraska";

            case 898:
                return "Closed due to border state request from Montana";

            case 897:
                return "Closed due to border state request from Idaho";

            case 896:
                return "Closed due to border state request from Colorado";

            default:
                return null;// default to returning null
        }
    }
}