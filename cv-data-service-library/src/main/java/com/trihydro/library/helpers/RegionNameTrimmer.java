package com.trihydro.library.helpers;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trihydro.library.model.RegionNameElementCollection;

@Component
@Slf4j
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
        int charactersToTrim = regionName.length() - MAX_REGION_NAME_LENGTH;
        String[] splitName = regionName.split("_");
        String direction = splitName[0];
        String route = splitName[1];
        String rsuOrSat = splitName[2];
        String timType = splitName[3];
        String timId = splitName[4];
        RegionNameElementCollection elements = new RegionNameElementCollection(direction, route, rsuOrSat, timType, timId);
        return trimRegionNameWithTimIdDelimiter(elements, charactersToTrim);
    }

    private String trimRegionNameWithTimIdDelimiter(RegionNameElementCollection elements, int charactersToTrim) {
        if (cannotBeTrimmedAndStillHaveRoomForEllipsis(elements.route, charactersToTrim)) {
            throw new IllegalArgumentException("Region name is too long and cannot be trimmed without unacceptable data loss");
        }

        log.info("Trimming 'route' part of region name of TIM to fit within 63 characters.");
        elements.route = elements.route.substring(0, elements.route.length() - (charactersToTrim + 3));
        return elements.direction + "_" + elements.route + "..." + "_" + elements.rsuOrSat + "_" + elements.timType + "_" + elements.timId;
    }

    private boolean cannotBeTrimmedAndStillHaveRoomForEllipsis(String route, int charactersToTrim) {
        return route.length() <= charactersToTrim + 3;
    }
}