package com.trihydro.library.model;

import java.util.HashMap;
import java.util.Map;

// Per SEMI_v2.3.0_070616 ASN.1 spec, there's room for additional SeqIDs
public enum SemiSequenceID {
    SvcReq(1), // Service request
    SvcResp(2), // Service response
    DataReq(3), // Data request
    DataConf(4), // Data confirmation
    Data(5), // Data
    Accept(6), // Acceptance
    Receipt(7), // Receipt
    SubscriptionReq(8), // Subscription Request
    SubscriptinoResp(9), // Subscription Response
    SubscriptionCancel(10); // Subscription Cancellation

    private int value;
    private static Map<Integer, SemiSequenceID> map = new HashMap<Integer, SemiSequenceID>();

    private SemiSequenceID(int value) {
        this.value = value;
    }

    static {
        for (SemiSequenceID ssId : SemiSequenceID.values()) {
            map.put(ssId.value, ssId);
        }
    }

    public static SemiSequenceID valueOf(int semiSequenceID) {
        return map.get(semiSequenceID);
    }

    public int getValue() {
        return value;
    }
}