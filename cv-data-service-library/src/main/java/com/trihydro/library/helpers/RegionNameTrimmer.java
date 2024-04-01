package com.trihydro.library.helpers;

import org.springframework.stereotype.Component;

import com.trihydro.library.service.CascadeService;

@Component
public class RegionNameTrimmer {
    private static final int MAX_REGION_NAME_LENGTH = 63;

    /**
     * Trims the region name if it is too long. Region names longer than 63 characters will fail to be processed by the ODE.
     * @param regionName The region name to trim
     * @return The trimmed region name
     */
    public String trimRegionNameIfTooLong(String regionName) {
        if (regionName == null) {
            return regionName;
        }
        if (exceedsMaxLength(regionName)) {
            return trimRegionName(regionName);
        }
        return regionName;
    }

    private boolean exceedsMaxLength(String regionName) {
        return regionName.length() > MAX_REGION_NAME_LENGTH;
    }

    private String trimRegionName(String regionName) {
        if (containsCascadeTimIdDelimiter(regionName)) {
            return trimRegionNameWithCascadeTimIdDelimiter(regionName);
        }
        String trimmedRegionName = regionName.substring(0, MAX_REGION_NAME_LENGTH - 3);
        String trimmedRegionNameWithEllipsis = addEllipsis(trimmedRegionName);
        return trimmedRegionNameWithEllipsis;
    }

    private String trimRegionNameWithCascadeTimIdDelimiter(String regionName) {
        int cascadeTimIdDelimiterIndex = regionName.indexOf(CascadeService.CASCADE_TIM_ID_DELIMITER);
        String stringBeforeCascadeTimIdDelimiter = regionName.substring(0, cascadeTimIdDelimiterIndex);
        String stringAfterCascadeTimIdDelimiter = regionName.substring(cascadeTimIdDelimiterIndex);
        String trimmedStringBeforeCascadeTimIdDelimiter = stringBeforeCascadeTimIdDelimiter.substring(0, (MAX_REGION_NAME_LENGTH - 3) - stringAfterCascadeTimIdDelimiter.length());
        String trimmedStringBeforeCascadeTimIdDelimiterWithEllipsis = addEllipsis(trimmedStringBeforeCascadeTimIdDelimiter);
        return trimmedStringBeforeCascadeTimIdDelimiterWithEllipsis + stringAfterCascadeTimIdDelimiter;
    }

    private boolean containsCascadeTimIdDelimiter(String regionName) {
        return regionName.contains(CascadeService.CASCADE_TIM_ID_DELIMITER);
    }

    private String addEllipsis(String regionName) {
        return regionName + "...";
    }
}
