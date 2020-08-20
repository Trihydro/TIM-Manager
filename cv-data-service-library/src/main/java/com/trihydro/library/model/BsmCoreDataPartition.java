package com.trihydro.library.model;

import java.util.Date;

public class BsmCoreDataPartition {
    public String partitionName;
    public Date highValue;

    public BsmCoreDataPartition() {

    }

    public BsmCoreDataPartition(String partitionName, Date highValue) {
        this.partitionName = partitionName;
        this.highValue = highValue;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Date getHighValue() {
        return highValue;
    }

    public void setHighValue(Date highValue) {
        this.highValue = highValue;
    }
}