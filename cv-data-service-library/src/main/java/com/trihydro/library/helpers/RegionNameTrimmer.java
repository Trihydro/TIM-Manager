package com.trihydro.library.helpers;

import org.springframework.stereotype.Component;

@Component
public class RegionNameTrimmer {
    private static final int MAX_REGION_NAME_LENGTH = 63;

    /**
     * Trims the region name if it is too long. Region names longer than 63 characters will fail to be processed by the ODE.
     * @param regionName The region name to trim
     * @return The trimmed region name
     */
    public String trimRegionNameIfTooLong(String regionName) {
        if (exceedsMaxLength(regionName)) {
            String trimmedRegionName = trimRegionName(regionName, MAX_REGION_NAME_LENGTH - 3);
            String trimmedRegionNameWithEllipsis = addEllipsis(trimmedRegionName);
            return trimmedRegionNameWithEllipsis;
        }
        return regionName;
    }

    private boolean exceedsMaxLength(String regionName) {
        return regionName.length() > MAX_REGION_NAME_LENGTH;
    }

    private String trimRegionName(String regionName, int targetLength) {
        return regionName.substring(0, targetLength);
    }

    private String addEllipsis(String regionName) {
        return regionName + "...";
    }
}
