package com.trihydro.tasks.models;

import java.util.List;

public abstract class SdxComparable implements Comparable<SdxComparable> {
    public abstract Integer getRecordId();

    public abstract List<Integer> getItisCodes();

    public abstract void setItisCodes(List<Integer> itisCodes);

    public int compareTo(SdxComparable o2) {
        // If o2 is null, then this (which isn't null) is greater
        if (o2 == null) {
            return 1;
        }

        // If both have invalid recordIds, then they're "equal"
        if (this.getRecordId() == null && o2.getRecordId() == null) {
            return 0;
        }

        // If this recordId is null, but o2's recordId ISN'T null (implied because we
        // didn't enter the previous if) then this is less than o2
        if (this.getRecordId() == null) {
            return -1;
        }

        // If o2 has a null recordId, but this doesn't (see above), then this is greater
        // than o2
        if (o2.getRecordId() == null) {
            return 1;
        }

        // At this point, we know both o1 and o2 don't have null recordIds. Compare.
        return this.getRecordId().compareTo(o2.getRecordId());
    }
}