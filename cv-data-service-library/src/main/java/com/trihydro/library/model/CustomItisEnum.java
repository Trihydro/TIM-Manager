package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

public enum CustomItisEnum {
    closedColorado(896), closedIdaho(897), closedMontana(898), closedNebraska(899), closedSouthDakota(900),
    closedUtah(901), closedMultipleStates(902), closedLaw(903), closedLocal(904), rollingClosure(912),
    winterCondCrash(905), localAccess(2567), noThroughTraffic(2571), noLightTrailers(2688), closedLHPV1(2689),
    closedLHPV2(2690), closedLHPV3(2691), closedLHPV4(2692), fire(3200), localCelebration(3968), hail(4880),
    slush(5914), winterConditions(6018), chainAllWheel(6272), blowOver(7040), wtchFallingRock(7296);

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
            case 896:
                return "Closed due to border state request from Colorado";
            case 897:
                return "Closed due to border state request from Idaho";
            case 898:
                return "Closed due to border state request from Montana";
            case 899:
                return "Closed due to border state request from Nebraska";
            case 900:
                return "Closed due to border state request from South Dakota";
            case 901:
                return "Closed due to border state request from Utah";
            case 902:
                return "Closed due to border state request from Multiple States";
            case 903:
                return "Closed due to law enforcement request";
            case 904:
                return "Closed due to local authority request";
            case 7040:
                return "Extreme blow over risk";
            case 2688:
                return "Advise no light trailers";
            case 2689:
                return "Closed to light, high profile vehicles";

            // codes from TMDD but not supported via documentation
            // case 905:
            // return "Winter conditions and crashes";
            // case 912:
            // return "rolling closure";
            // case 2567:
            // return "Local access only";
            // case 2571:
            // return "No through traffic";
            // case 2690:
            // return "Closed to light, high-profile vehicles due to active blowovers in the
            // area";
            // case 2691:
            // return "Closed to light, high-profile vehicles due to gusting winds";
            // case 2692:
            // return "Closed to light, high-profile vehicles due to gusting winds and slick
            // conditions";
            // case 3200:
            // return "Fire";
            // case 3968:
            // return "Local celebration";
            // case 4880:
            // return "Hail";
            // case 5914:
            // return "Slush";
            // case 6018:
            // return "Winter conditions";
            // case 6272:
            // return "Chains or all wheel drive with snow tires required";
            // case 7296:
            // return "Watch for falling rock";

            default:
                return null;// default to returning null
        }
    }
}