package com.trihydro.library.helpers.caches;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.JCSCacheProps;
import com.trihydro.library.model.TriggerRoad;

@ExtendWith(MockitoExtension.class)
public class TriggerRoadCacheTest {
    @Mock
    private static Utility mockUtility;

    @Mock
    private static JCSCacheProps mockConfig;
    
    @Test
    public void testInitialization_Success() {
        // prepare
        mockUtility = new Utility();
        mockConfig = new JCSCachePropsTestingImpl();

        // execute
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility, mockConfig);

        // verify
        assertNotNull(triggerRoadCache);

        // cleanup
        triggerRoadCache.shutdown();
    }

    @Test
    public void testGetSegmentIdsAssociatedWithTriggerRoad_NotFound() {
        // prepare
        mockUtility = new Utility();
        mockConfig = new JCSCachePropsTestingImpl();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility, mockConfig);

        // execute
        List<Integer> segmentIds = triggerRoadCache.getSegmentIdsAssociatedWithTriggerRoad("I80");
        
        // verify null
        assertNull(segmentIds);

        // cleanup
        triggerRoadCache.shutdown();
    }

    @Test
    public void testGetSegmentIdsAssociatedWithTriggerRoad_Found() {
        // prepare
        mockUtility = new Utility();
        mockConfig = new JCSCachePropsTestingImpl();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility, mockConfig);
        TriggerRoad triggerRoad = new TriggerRoad("I80");
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, false, false, false, false);
        triggerRoad.addCountyRoadSegment(countyRoadSegment);
        triggerRoadCache.updateCache("I80", triggerRoad);

        // execute
        List<Integer> segmentIds = triggerRoadCache.getSegmentIdsAssociatedWithTriggerRoad("I80");
        
        // verify
        assertNotNull(segmentIds);
        assert(segmentIds.size() > 0);

        // cleanup
        triggerRoadCache.shutdown();
    }

    @Test
    public void testClear() {
        // prepare
        mockUtility = new Utility();
        mockConfig = new JCSCachePropsTestingImpl();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility, mockConfig);
        TriggerRoad triggerRoad = new TriggerRoad("I80");
        triggerRoadCache.updateCache("I80", triggerRoad);

        // execute & verify
        triggerRoadCache.clear();
        List<Integer> segmentIds = triggerRoadCache.getSegmentIdsAssociatedWithTriggerRoad("I80");
        assertNull(segmentIds);
    }

    // JCSCacheProps implementation for testing
    private static class JCSCachePropsTestingImpl implements JCSCacheProps {

        @Override
        public String getJcsDefault() {
            return "";
        }

        @Override
        public void setJcsDefault(String jcsDefault) {
            // unused
        }

        @Override
        public String getCacheAttributes() {
            return "org.apache.jcs.engine.CompositeCacheAttributes";
        }

        @Override
        public void setCacheAttributes(String cacheAttributes) {
            // unused
        }

        @Override
        public String getMaxObjects() {
            return "1000";
        }

        @Override
        public void setMaxObjects(String maxObjects) {
            // unused
        }

        @Override
        public String getMemoryCacheName() {
            return "org.apache.jcs.engine.memory.lru.LRUMemoryCache";
        }

        @Override
        public void setMemoryCacheName(String memoryCacheName) {
            // unused
        }

        @Override
        public String getUseMemoryShrinker() {
            return "true";
        }

        @Override
        public void setUseMemoryShrinker(String useMemoryShrinker) {
            // unused
        }

        @Override
        public String getMaxMemoryIdleTimeSeconds() {
            return "3600";
        }

        @Override
        public void setMaxMemoryIdleTimeSeconds(String maxMemoryIdleTimeSeconds) {
            // unused
        }

        @Override
        public String getShrinkerIntervalSeconds() {
            return "60";
        }

        @Override
        public void setShrinkerIntervalSeconds(String shrinkerIntervalSeconds) {
            // unused
        }

        @Override
        public String getMaxSpoolPerRun() {
            return "500";
        }

        @Override
        public void setMaxSpoolPerRun(String maxSpoolPerRun) {
            // unused
        }

        @Override
        public String getElementAttributes() {
            return "org.apache.jcs.engine.ElementAttributes";
        }

        @Override
        public void setElementAttributes(String elementAttributes) {
            // unused
        }

        @Override
        public String getIsEternal() {
            return "false";
        }

        @Override
        public void setIsEternal(String isEternal) {
            // unused
        }

        @Override
        public String getMaxLife() {
            return "3600";
        }

        @Override
        public void setMaxLife(String maxLife) {
            // unused
        }

        @Override
        public String getIsSpool() {
            return "true";
        }

        @Override
        public void setIsSpool(String isSpool) {
            // unused
        }

        @Override
        public String getIsRemote() {
            return "false";
        }

        @Override
        public void setIsRemote(String isRemote) {
            // unused
        }

        @Override
        public String getIsLateral() {
            return "false";
        }

        @Override
        public void setIsLateral(String isLateral) {
            // unused
        }
    }
}