package com.trihydro.library.helpers.caches;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.helpers.caches.TriggerRoadCache.NotCachedException;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.TriggerRoad;

@ExtendWith(MockitoExtension.class)
public class TriggerRoadCacheTest {
    @Mock
    private static Utility mockUtility;
    
    @Test
    public void testInitialization_Success() {
        // prepare
        mockUtility = new Utility();

        // execute
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility);

        // verify
        assertNotNull(triggerRoadCache);

        // cleanup
        triggerRoadCache.shutdown();
    }

    @Test
    public void testGetSegmentIdsAssociatedWithTriggerRoad_NotFound() {
        // prepare
        mockUtility = new Utility();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility);

        // execute
        List<Integer> segmentIds = null;
        try {
            segmentIds = triggerRoadCache.getSegmentIdsAssociatedWithTriggerRoad("I80");
        } catch (NotCachedException e) {
            // ignore
        }
        
        // verify null
        assertNull(segmentIds);

        // cleanup
        triggerRoadCache.shutdown();
    }

    @Test
    public void testGetSegmentIdsAssociatedWithTriggerRoad_Found() {
        // prepare
        mockUtility = new Utility();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility);
        TriggerRoad triggerRoad = new TriggerRoad("I80");
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, false, false, false, false);
        triggerRoad.addCountyRoadSegment(countyRoadSegment);
        triggerRoadCache.updateCache("I80", triggerRoad);

        // execute
        List<Integer> segmentIds = null;
        try {
            segmentIds = triggerRoadCache.getSegmentIdsAssociatedWithTriggerRoad("I80");
        } catch (NotCachedException e) {
            // ignore
        }
        
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
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility);
        TriggerRoad triggerRoad = new TriggerRoad("I80");
        triggerRoadCache.updateCache("I80", triggerRoad);

        // execute & verify
        triggerRoadCache.clear();
        List<Integer> segmentIds = null;
        try {
            segmentIds = triggerRoadCache.getSegmentIdsAssociatedWithTriggerRoad("I80");
        } catch (NotCachedException e) {
            assertNotNull(e);
        }
        assertNull(segmentIds);
    }

}
