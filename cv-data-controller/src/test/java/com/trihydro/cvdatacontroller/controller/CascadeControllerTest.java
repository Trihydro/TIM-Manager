package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.trihydro.cvdatacontroller.model.Milepost;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.JCSCacheProps;
import com.trihydro.library.model.TriggerRoad;

public class CascadeControllerTest extends TestBase<CascadeController> {

    private JCSCacheProps mockJCSCacheProps;

    private CountyRoadSegment createCountyRoadSegment() {
        int countyRoadId = 1;
        String commonName = "test";
        Double mFrom = 1.0;
        Double mTo = 2.0;
        Double xFrom = 3.0;
        Double yFrom = 4.0;
        Double xTo = 5.0;
        Double yTo = 6.0;
        boolean closed = false;
        boolean c2lhpv = false;
        boolean loct = false;
        boolean ntt = false;
        return new CountyRoadSegment(countyRoadId, commonName, mFrom, mTo, xFrom, yFrom, xTo, yTo, closed, c2lhpv, loct, ntt);
    }

    @BeforeEach
    public void setupSubTest() throws SQLException {
        mockJCSCacheProps = mock(JCSCacheProps.class);
        doReturn("").when(mockJCSCacheProps).getJcsDefault();
        doReturn("org.apache.commons.jcs3.engine.CompositeCacheAttributes").when(mockJCSCacheProps).getCacheAttributes();
        doReturn("1000").when(mockJCSCacheProps).getMaxObjects();
        doReturn("org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache").when(mockJCSCacheProps).getMemoryCacheName();
        doReturn("true").when(mockJCSCacheProps).getUseMemoryShrinker();
        doReturn("3600").when(mockJCSCacheProps).getMaxMemoryIdleTimeSeconds();
        doReturn("60").when(mockJCSCacheProps).getShrinkerIntervalSeconds();
        doReturn("500").when(mockJCSCacheProps).getMaxSpoolPerRun();
        doReturn("org.apache.commons.jcs3.engine.ElementAttributes").when(mockJCSCacheProps).getElementAttributes();
        doReturn("false").when(mockJCSCacheProps).getIsEternal();
        doReturn("3600").when(mockJCSCacheProps).getMaxLife();
        doReturn("true").when(mockJCSCacheProps).getIsSpool();
        doReturn("false").when(mockJCSCacheProps).getIsRemote();
        doReturn("false").when(mockJCSCacheProps).getIsLateral();
        uut.InjectBaseDependencies(mockUtility, mockJCSCacheProps);
    }

    @Test
    public void testGetTriggerRoad_CacheHit_SegmentsFound_SUCCESS() throws SQLException {
        // prepare
        String roadCode = "test";
        CountyRoadSegment countyRoadSegment = createCountyRoadSegment();
        List<CountyRoadSegment> countyRoadSegments = new ArrayList<>();
        countyRoadSegments.add(countyRoadSegment);
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, countyRoadSegments);
        uut.addToCache(triggerRoad); // add to cache to ensure cache hit
        doReturn("test").when(mockRs).getString("common_name");

        // execute
        ResponseEntity<TriggerRoad> data = uut.getTriggerRoad(roadCode);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, data.getBody().getRoadCode());
    }

    @Test
    public void testGetTriggerRoad_CacheHit_NoSegmentsFound_SUCCESS() {
        // prepare
        String roadCode = "test";
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, new ArrayList<>());
        uut.addToCache(triggerRoad); // add to cache to ensure cache hit

        // execute
        ResponseEntity<TriggerRoad> data = uut.getTriggerRoad(roadCode);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, data.getBody().getRoadCode());
    }

    @Test
    public void testGetTriggerRoad_CacheMiss_SegmentsFound_SUCCESS() throws SQLException {
        // prepare
        String roadCode = "test";
        CountyRoadSegment countyRoadSegment = createCountyRoadSegment();
        List<CountyRoadSegment> countyRoadSegments = new ArrayList<>();
        countyRoadSegments.add(countyRoadSegment);
        doReturn("test").when(mockRs).getString("common_name");
        uut.clearCache(); // clear cache to ensure cache miss
        
        // execute
        ResponseEntity<TriggerRoad> data = uut.getTriggerRoad(roadCode);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, data.getBody().getRoadCode());
    }

    @Test
    public void testGetTriggerRoad_CacheMiss_NoSegmentsFound_SUCCESS() throws SQLException {
        // prepare
        String roadCode = "test";
        uut.clearCache(); // clear cache to ensure cache miss
        
        // execute
        ResponseEntity<TriggerRoad> data = uut.getTriggerRoad(roadCode);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, data.getBody().getRoadCode());
    }

    @Test
    public void testGetTriggerRoad_CacheMiss_SQLException_FAILURE() throws SQLException {
        // prepare
        String roadCode = "test";
        doThrow(new SQLException()).when(mockRs).getString("common_name");
        uut.clearCache(); // clear cache to ensure cache miss

        // execute
        ResponseEntity<TriggerRoad> data = uut.getTriggerRoad(roadCode);

        // verify
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertNull(data.getBody());
    }

    @Test
    public void testGetMileposts_NoMileposts_SUCCESS() {
        // prepare
        int countyRoadId = 1;

        // execute
        ResponseEntity<List<Milepost>> data = uut.getMileposts(countyRoadId);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
    }

    @Test
    public void testGetMileposts_MilepostsFound_SUCCESS() throws SQLException {
        // prepare
        int countyRoadId = 1;
        doReturn("test").when(mockRs).getString("common_name");
        
        // execute
        ResponseEntity<List<Milepost>> data = uut.getMileposts(countyRoadId);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
    }

    @Test
    public void testGetMileposts_SQLException_FAILURE() throws SQLException {
        // prepare
        int countyRoadId = 1;
        doThrow(new SQLException()).when(mockRs).getString("common_name");
        
        // execute
        ResponseEntity<List<Milepost>> data = uut.getMileposts(countyRoadId);

        // verify
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
    }
}