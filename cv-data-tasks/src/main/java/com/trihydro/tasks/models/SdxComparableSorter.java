package com.trihydro.tasks.models;

import java.util.Comparator;

public class SdxComparableSorter implements Comparator<ISdxComparable> {

    @Override
    public int compare(ISdxComparable o1, ISdxComparable o2) {
        if ((o1 == null || o1.getRecordId() == null) && (o2 == null || o2.getRecordId() == null)) {
            return 0;
        }
        if (o1 == null || o1.getRecordId() == null) {
            return -1;
        }
        if (o2 == null || o2.getRecordId() == null) {
            return 1;
        }

        return o1.getRecordId().compareTo(o2.getRecordId());
    }
}