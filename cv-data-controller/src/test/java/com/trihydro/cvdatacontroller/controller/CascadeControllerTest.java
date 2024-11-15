package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.trihydro.cvdatacontroller.model.Milepost;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.CountyRoadsProps;
import com.trihydro.library.model.JCSCacheProps;
import com.trihydro.library.model.TriggerRoad;

public class CascadeControllerTest extends TestBase<CascadeController> {

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
        JCSCacheProps mockJCSCacheProps = mock(JCSCacheProps.class);
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

        CountyRoadsProps mockCountyRoadsProps = mock(CountyRoadsProps.class);
        uut.InjectBaseDependencies(mockUtility, mockJCSCacheProps, mockCountyRoadsProps);
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
        doReturn("test").when(mockRs).getString("name");

        // execute
        ResponseEntity<String> responseJson = uut.getTriggerRoad(roadCode);
        TriggerRoad result = TriggerRoad.fromJson(responseJson.getBody());

        // verify
        Assertions.assertEquals(HttpStatus.OK, responseJson.getStatusCode());
        Assertions.assertEquals(1, result.getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, result.getRoadCode());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions).getCountyRoadsConnectionPool();
        Mockito.verify(mockConnectionCountyRoads).close();
    }

    @Test
    public void testGetTriggerRoad_CacheHit_NoSegmentsFound_SUCCESS() throws SQLException {
        // prepare
        String roadCode = "test";
        TriggerRoad triggerRoad = new TriggerRoad(roadCode, new ArrayList<>());
        uut.addToCache(triggerRoad); // add to cache to ensure cache hit

        // execute
        ResponseEntity<String> responseJson = uut.getTriggerRoad(roadCode);
        TriggerRoad result = TriggerRoad.fromJson(responseJson.getBody());

        // verify
        Assertions.assertEquals(HttpStatus.OK, responseJson.getStatusCode());
        Assertions.assertEquals(0, result.getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, result.getRoadCode());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions, Mockito.never()).getCountyRoadsConnectionPool();
    }

    @Test
    public void testGetTriggerRoad_CacheMiss_SegmentsFound_SUCCESS() throws SQLException {
        // prepare
        String roadCode = "test";
        doReturn("test").when(mockRs).getString("name");
        uut.clearCache(); // clear cache to ensure cache miss
        
        // execute
        ResponseEntity<String> responseJson = uut.getTriggerRoad(roadCode);
        TriggerRoad result = TriggerRoad.fromJson(responseJson.getBody());

        // verify
        Assertions.assertEquals(HttpStatus.OK, responseJson.getStatusCode());
        Assertions.assertEquals(1, result.getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, result.getRoadCode());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions).getCountyRoadsConnectionPool();
        Mockito.verify(mockConnectionCountyRoads).close();
    }

    @Test
    public void testGetTriggerRoad_CacheMiss_NoSegmentsFound_SUCCESS() throws SQLException {
        // prepare
        String roadCode = "test";
        uut.clearCache(); // clear cache to ensure cache miss
        
        // execute
        ResponseEntity<String> responseJson = uut.getTriggerRoad(roadCode);
        TriggerRoad result = TriggerRoad.fromJson(responseJson.getBody());

        // verify
        Assertions.assertEquals(HttpStatus.OK, responseJson.getStatusCode());
        Assertions.assertEquals(0, result.getCountyRoadSegments().size());
        Assertions.assertEquals(roadCode, result.getRoadCode());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions).getCountyRoadsConnectionPool();
        Mockito.verify(mockConnectionCountyRoads).close();
    }

    @Test
    public void testGetTriggerRoad_CacheMiss_SQLException_FAILURE() throws SQLException {
        // prepare
        String roadCode = "test";
        doThrow(new SQLException()).when(mockRs).getString("name");
        uut.clearCache(); // clear cache to ensure cache miss

        // execute
        ResponseEntity<String> responseJson = uut.getTriggerRoad(roadCode);

        // verify
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseJson.getStatusCode());
        Assertions.assertNull(responseJson.getBody());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions).getCountyRoadsConnectionPool();
        Mockito.verify(mockConnectionCountyRoads).close();
    }

    @Test
    public void testGetMileposts_NoMileposts_SUCCESS() throws SQLException {
        // prepare
        int countyRoadId = 1;

        // execute
        ResponseEntity<List<Milepost>> data = uut.getMileposts(countyRoadId);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(0, Objects.requireNonNull(data.getBody()).size());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions).getCountyRoadsConnectionPool();
        Mockito.verify(mockConnectionCountyRoads).close();
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
        Assertions.assertEquals(1, Objects.requireNonNull(data.getBody()).size());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions).getCountyRoadsConnectionPool();
        Mockito.verify(mockConnectionCountyRoads).close();
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
        Assertions.assertEquals(0, Objects.requireNonNull(data.getBody()).size());
        Mockito.verify(mockDbInteractions, Mockito.never()).getConnectionPool();
        Mockito.verify(mockDbInteractions).getCountyRoadsConnectionPool();
        Mockito.verify(mockConnectionCountyRoads).close();
    }

    @Test
    public void testGetActiveTimsWithItisCodesForSegment_SUCCESS() throws SQLException {
        // prepare
        int countyRoadId = 1;
        String clientId = "test";
        doReturn(1L).when(mockRs).getLong("ACTIVE_TIM_ID");
        doReturn(1L).when(mockRs).getLong("TIM_ID");
        doReturn("test").when(mockRs).getString("DIRECTION");
        doReturn("test").when(mockRs).getString("TIM_START");
        doReturn(null).when(mockRs).getString("TIM_END");
        doReturn("test").when(mockRs).getString("EXPIRATION_DATE");
        doReturn("test").when(mockRs).getString("ROUTE");
        doReturn(clientId).when(mockRs).getString("CLIENT_ID");
        doReturn("test").when(mockRs).getString("SAT_RECORD_ID");
        doReturn(1).when(mockRs).getInt("PK");
        doReturn(new BigDecimal(1)).when(mockRs).getBigDecimal("START_LATITUDE");
        doReturn(new BigDecimal(1)).when(mockRs).getBigDecimal("START_LONGITUDE");
        doReturn(new BigDecimal(1)).when(mockRs).getBigDecimal("END_LATITUDE");
        doReturn(new BigDecimal(1)).when(mockRs).getBigDecimal("END_LONGITUDE");
        doReturn(1L).when(mockRs).getLong("TIM_TYPE_ID");
        doReturn("test").when(mockRs).getString("TYPE");
        doReturn(1).when(mockRs).getInt("PROJECT_KEY");
        doReturn(769).when(mockRs).getInt("ITIS_CODE");

        // execute
        ResponseEntity<List<ActiveTim>> data = uut.getActiveTimsWithItisCodesForSegment(countyRoadId);

        // verify
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertNotNull(data.getBody());
        Assertions.assertEquals(1, data.getBody().size());
        Mockito.verify(mockDbInteractions).getConnectionPool();
        Mockito.verify(mockConnection).close();
        Mockito.verify(mockDbInteractions, Mockito.never()).getCountyRoadsConnectionPool();
    }

    @Test
    public void testGetActiveTimsWithItisCodesForSegment_SQLException_FAILURE() throws SQLException {
        // prepare
        int countyRoadId = 1;
        doThrow(new SQLException()).when(mockRs).getString("DIRECTION");
        
        // execute
        ResponseEntity<List<ActiveTim>> data = uut.getActiveTimsWithItisCodesForSegment(countyRoadId);

        // verify
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertNull(data.getBody());
        Mockito.verify(mockDbInteractions).getConnectionPool();
        Mockito.verify(mockConnection).close();
        Mockito.verify(mockDbInteractions, Mockito.never()).getCountyRoadsConnectionPool();
    }
}