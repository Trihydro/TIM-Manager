package com.trihydro.library.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.TriggerRoad;

public class TriggerRoadTest {
    
    @Test
    public void testInitialization() {
        // prepare
        String roadCode = "example road code";
        List<CountyRoadSegment> countyRoadSegments = new ArrayList<>();
        
        // execute
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, countyRoadSegments);

        // verify
        assertNotNull(triggerRoad);
    }

    @Test
    public void testGetCountyRoadSegments() {
        // prepare
        String roadCode = "example road code";
        List<CountyRoadSegment> countyRoadSegments = new ArrayList<>();
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, true, true, true, true);
        countyRoadSegments.add(countyRoadSegment);

        // execute
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, countyRoadSegments);
        List<CountyRoadSegment> countyRoadSegmentsFound = triggerRoad.getCountyRoadSegments();

        // verify
        assertEquals(countyRoadSegments, countyRoadSegmentsFound);
    }
}
