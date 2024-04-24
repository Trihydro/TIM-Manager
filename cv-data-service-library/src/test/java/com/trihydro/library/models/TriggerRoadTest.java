package com.trihydro.library.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
    public void testSerialization_NoSegments() throws JsonProcessingException {
        // prepare
        String roadCode = "example road code";
        TriggerRoad triggerRoad = new TriggerRoad(roadCode);
        String expectedJson = "{\"roadCode\":\"example road code\",\"countyRoadSegments\":[]}";

        // execute
        String json = triggerRoad.toJson();

        // verify
        assertEquals(expectedJson, json);
    }

    @Test
    public void testSerialization_OneSegment() throws JsonProcessingException {
        // prepare
        String roadCode = "example road code";
        List<CountyRoadSegment> countyRoadSegments = new ArrayList<>();
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, true, true, true, true);
        countyRoadSegments.add(countyRoadSegment);
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, countyRoadSegments);
        String expectedJson = "{\"roadCode\":\"example road code\",\"countyRoadSegments\":[{\"countyRoadId\":1,\"commonName\":\"example common name\",\"mFrom\":1.0,\"mTo\":2.0,\"xFrom\":3.0,\"yFrom\":4.0,\"xTo\":5.0,\"yTo\":6.0,\"closed\":true,\"c2lhpv\":true,\"loct\":true,\"ntt\":true}]}";

        // execute
        String json = triggerRoad.toJson();

        // verify
        assertEquals(expectedJson, json);
    }

    @Test
    public void testDeserialization_NoSegments() throws JsonMappingException, JsonProcessingException {
        // prepare
        String json = "{\"roadCode\":\"I 80\",\"countyRoadSegments\":[]}";
        TriggerRoad expectedTriggerRoad = new TriggerRoad("I 80");

        // execute
        TriggerRoad triggerRoad = TriggerRoad.fromJson(json);

        // verify
        assertEquals(expectedTriggerRoad.getRoadCode(), triggerRoad.getRoadCode());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().size(), triggerRoad.getCountyRoadSegments().size());
    }

    @Test
    public void testDeserialization_OneSegment() throws JsonMappingException, JsonProcessingException {
        // prepare
        String json = "{\"roadCode\":\"example road code\",\"countyRoadSegments\":[{\"countyRoadId\":1,\"commonName\":\"example common name\",\"mFrom\":1.0,\"mTo\":2.0,\"xFrom\":3.0,\"yFrom\":4.0,\"xTo\":5.0,\"yTo\":6.0,\"closed\":true,\"c2lhpv\":true,\"loct\":true,\"ntt\":true}]}";
        TriggerRoad expectedTriggerRoad = new TriggerRoad("example road code");
        expectedTriggerRoad.addCountyRoadSegment(new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, true, true, true, true));

        // execute
        TriggerRoad triggerRoad = TriggerRoad.fromJson(json);

        // verify
        assertEquals(expectedTriggerRoad.getRoadCode(), triggerRoad.getRoadCode());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().size(), triggerRoad.getCountyRoadSegments().size());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getId(), triggerRoad.getCountyRoadSegments().get(0).getId());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getCommonName(), triggerRoad.getCountyRoadSegments().get(0).getCommonName());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getMFrom(), triggerRoad.getCountyRoadSegments().get(0).getMFrom());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getMTo(), triggerRoad.getCountyRoadSegments().get(0).getMTo());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getXFrom(), triggerRoad.getCountyRoadSegments().get(0).getXFrom());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getYFrom(), triggerRoad.getCountyRoadSegments().get(0).getYFrom());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getXTo(), triggerRoad.getCountyRoadSegments().get(0).getXTo());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).getYTo(), triggerRoad.getCountyRoadSegments().get(0).getYTo());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).isClosed(), triggerRoad.getCountyRoadSegments().get(0).isClosed());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).isC2lhpv(), triggerRoad.getCountyRoadSegments().get(0).isC2lhpv());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).isLoct(), triggerRoad.getCountyRoadSegments().get(0).isLoct());
        assertEquals(expectedTriggerRoad.getCountyRoadSegments().get(0).isNtt(), triggerRoad.getCountyRoadSegments().get(0).isNtt());
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

    @Test
    public void testGetCountyRoadSegmentIds() {
        // prepare
        String roadCode = "example road code";
        List<CountyRoadSegment> countyRoadSegments = new ArrayList<>();
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, true, true, true, true);
        countyRoadSegments.add(countyRoadSegment);
        List<Integer> expectedCountyRoadSegmentIds = new ArrayList<>();
        expectedCountyRoadSegmentIds.add(countyRoadSegment.getId());

        // execute
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, countyRoadSegments);
        List<Integer> countyRoadSegmentIds = triggerRoad.getCountyRoadSegmentIds();

        // verify
        assertEquals(1, countyRoadSegmentIds.size());
        assertEquals(expectedCountyRoadSegmentIds, countyRoadSegmentIds);
    }

    @Test
    public void testHasCountyRoadSegments() {
        // prepare
        String roadCode = "example road code";
        List<CountyRoadSegment> countyRoadSegments = new ArrayList<>();
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, true, true, true, true);
        countyRoadSegments.add(countyRoadSegment);

        // execute
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, countyRoadSegments);
        boolean hasCountyRoadSegments = triggerRoad.hasCountyRoadSegments();

        // verify
        assertEquals(true, hasCountyRoadSegments);
    }
}
