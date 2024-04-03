package com.trihydro.library.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trihydro.library.service.CascadeService;

@Component
public class RegionNameTrimmer {
    private static final int MAX_REGION_NAME_LENGTH = 63;
    
    private Utility utility;

    @Autowired
    public RegionNameTrimmer(Utility _utility) {
        utility = _utility;
    }

    /**
     * Trims the region name if it is too long. Region names longer than 63 characters will fail to be processed by the ODE.
     * @param regionName The region name to trim
     * @return The trimmed region name
     */
    public String trimRegionNameIfTooLong(String regionName) throws IllegalArgumentException {
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
        if (!containsCascadeTimIdDelimiter(regionName)) {
            throw new IllegalArgumentException("No cascade TIM ID delimiter found in region name, indicating that this is for a regular TIM and should not be trimmed");
        }
        int charactersToTrim = regionName.length() - MAX_REGION_NAME_LENGTH;
        String[] splitName = regionName.split("_");
        String direction = splitName[0];
        String route = splitName[1];
        String rsuOrSat = splitName[2];
        String timType = splitName[3];
        String timId = splitName[4];
        String cascadeTimIdDelimiter = splitName[5];
        String cascadeTimId = splitName[6];
        RegionNameElementCollection elements = new RegionNameElementCollection(direction, route, rsuOrSat, timType, timId, cascadeTimIdDelimiter, cascadeTimId);
        return trimRegionNameWithCascadeTimIdDelimiter(elements, charactersToTrim);
    }

    private String trimRegionNameWithCascadeTimIdDelimiter(RegionNameElementCollection elements, int charactersToTrim) {
        if (cannotBeTrimmedAndStillHaveRoomForEllipsis(elements.route, charactersToTrim)) {
            throw new IllegalArgumentException("Region name is too long and cannot be trimmed without unacceptable data loss");
        }
        
        utility.logWithDate("Trimming 'route' part of region name of cascade TIM to fit within 63 characters.");
        elements.route = elements.route.substring(0, elements.route.length() - (charactersToTrim + 3));
        return elements.direction + "_" + elements.route + "..." + "_" + elements.rsuOrSat + "_" + elements.timType + "_" + elements.timId + "_" + elements.cascadeTimIdDelimiter + "_" + elements.cascadeTimId;
    }

    private boolean cannotBeTrimmedAndStillHaveRoomForEllipsis(String route, int charactersToTrim) {
        return route.length() <= charactersToTrim + 3;
    }

    private boolean containsCascadeTimIdDelimiter(String regionName) {
        return regionName.contains(CascadeService.CASCADE_TIM_ID_DELIMITER);
    }

    private class RegionNameElementCollection {
        public String direction;
        public String route;
        public String rsuOrSat;
        public String timType;
        public String timId;
        public String cascadeTimIdDelimiter;
        public String cascadeTimId;

        public RegionNameElementCollection(String direction, String route, String rsuOrSat, String timType, String timId, String cascadeTimIdDelimiter, String cascadeTimId) {
            this.direction = direction;
            this.route = route;
            this.rsuOrSat = rsuOrSat;
            this.timType = timType;
            this.timId = timId;
            this.cascadeTimIdDelimiter = cascadeTimIdDelimiter;
            this.cascadeTimId = cascadeTimId;
        }
    }
}
