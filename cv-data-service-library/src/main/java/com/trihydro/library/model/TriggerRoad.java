package com.trihydro.library.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/**
 * A trigger road is a road that triggers cascading conditions on a number of county road segments.
 */
public class TriggerRoad implements Serializable {
    private String roadCode;
    private List<CountyRoadSegment> countyRoadSegments;

    public TriggerRoad(String roadCode) {
        this.roadCode = roadCode;
        this.countyRoadSegments = new ArrayList<CountyRoadSegment>();
    }

    public TriggerRoad(String roadCode, List<CountyRoadSegment> countyRoadSegments) {
        this.roadCode = roadCode;
        this.countyRoadSegments = countyRoadSegments;
    }

    public String getRoadCode() {
        return roadCode;
    }

    public List<CountyRoadSegment> getCountyRoadSegments() {
        return countyRoadSegments;
    }

    public void addCountyRoadSegment(CountyRoadSegment countyRoadSegment) {
        countyRoadSegments.add(countyRoadSegment);
    }

    public List<Integer> getCountyRoadSegmentIds() {
        List<Integer> countyRoadSegmentIds = new ArrayList<Integer>();
        for (CountyRoadSegment countyRoadSegment : countyRoadSegments) {
            countyRoadSegmentIds.add(countyRoadSegment.getId());
        }
        return countyRoadSegmentIds;
    }

    public boolean hasCountyRoadSegments() {
        return countyRoadSegments.size() > 0;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static TriggerRoad fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, TriggerRoad.class);
    }
}
