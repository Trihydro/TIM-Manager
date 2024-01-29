package com.trihydro.library.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A trigger road is a road that triggers cascading conditions on a number of county road segments.
 */
public class TriggerRoad implements Serializable {
    private String roadCode;
    private List<CountyRoadSegment> countyRoadSegments;

    public TriggerRoad() {
        this.roadCode = "";
        this.countyRoadSegments = new ArrayList<CountyRoadSegment>();
    }

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
}
