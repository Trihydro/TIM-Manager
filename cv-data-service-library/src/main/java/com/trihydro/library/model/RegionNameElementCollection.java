package com.trihydro.library.model;

public class RegionNameElementCollection {
    public String direction;
    public String route;
    public String rsuOrSat;
    public String timType;
    public String timId;
    public String pk;

    public RegionNameElementCollection() {

    }

    public RegionNameElementCollection(String regionName) {
        String[] splitName = regionName.split("_");
        if (splitName.length == 0) {
            return;
        }
        this.direction = splitName[0];

        if (splitName.length > 1) {
            this.route = splitName[1];
        }
        else {
            return;
        }

        if (splitName.length > 2) {
            this.rsuOrSat = splitName[2];
        }
        else {
            return;
        }

        if (splitName.length > 3) {
            this.timType = splitName[3];
        }
        else {
            return;
        }

        if (splitName.length > 4) {
            this.timId = splitName[4];
        }
        else {
            return;
        }

        if (splitName.length > 5) {
            this.pk = splitName[5];
        }
        else {
            return;
        }
    }

    public RegionNameElementCollection (String direction, String route, String rsuOrSat, String timType, String timId) {
        this.direction = direction;
        this.route = route;
        this.rsuOrSat = rsuOrSat;
        this.timType = timType;
        this.timId = timId;
    }
}
