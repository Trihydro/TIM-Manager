package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

// Per SEMI_v2.3.0_070616 ASN.1 spec, there's room for additional DialogIDs
public enum SemiDialogID {
    VehSitData(154), // 0x009A Vehicle Situation Data Deposit
    DataSubscription(155), // 0x009B Data Subscription
    AdvSitDataDep(156), // 0x009C Advisory Situation Data Deposit
    AdvSitDatDist(157), // 0x009D Advisory Situation Data Distribution
    Reserved1(158), // 0x009E
    Reserved2(159), // 0x009F
    ObjReg(160), // 0x00A0 Object Registration
    ObjDisc(161), // 0x00A1 Object Discovery
    IntersectionSitDataDep(162), // 0x00A2 Intersection Situation Data Deposit
    IntersectionSitDataQuery(163); // 0x00A3 Intersection Situation Data Query

    private int value;
    private static Map<Integer, SemiDialogID> map = new HashMap<Integer, SemiDialogID>();

    private SemiDialogID(int value) {
        this.value = value;
    }

    static {
        for (SemiDialogID sdId : SemiDialogID.values()) {
            map.put(sdId.value, sdId);
        }
    }

    public static SemiDialogID valueOf(int semiDialogId) {
        return map.get(semiDialogId);
    }

    public int getValue() {
        return value;
    }
}