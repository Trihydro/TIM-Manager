package com.trihydro.timrefresh.Models;

import java.util.List;

public class ManualRefresh {
    
    private List<Long> activeTimIds;
    public List<Long> getActiveTimIds() {
        return activeTimIds;
    }
    public void setActiveTimIds(List<Long> activeTimIds) {
        this.activeTimIds = activeTimIds;
    }
}
