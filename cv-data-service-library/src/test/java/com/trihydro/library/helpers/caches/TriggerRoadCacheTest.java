package com.trihydro.library.helpers.caches;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trihydro.library.helpers.Utility;
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
    public void testGetTriggerRoad_NotFound() {
        // prepare
        mockUtility = new Utility();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility);

        // execute
        TriggerRoad triggerRoad = triggerRoadCache.getTriggerRoad("I80");
        
        // verify null
        assertNull(triggerRoad);

        // cleanup
        triggerRoadCache.shutdown();
    }

    @Test
    public void testGetTriggerRoad_Found() {
        // prepare
        mockUtility = new Utility();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility);
        TriggerRoad triggerRoad = new TriggerRoad("I80", null);
        triggerRoadCache.updateCache("I80", triggerRoad);

        // execute
        TriggerRoad triggerRoadFound = triggerRoadCache.getTriggerRoad("I80");
        
        // verify
        assertNotNull(triggerRoadFound);

        // cleanup
        triggerRoadCache.shutdown();
    }

    @Test
    public void testClear() {
        // prepare
        mockUtility = new Utility();
        TriggerRoadCache triggerRoadCache = new TriggerRoadCache(mockUtility);
        TriggerRoad triggerRoad = new TriggerRoad("I80", null);
        triggerRoadCache.updateCache("I80", triggerRoad);

        // execute
        triggerRoadCache.clear();
        TriggerRoad triggerRoadFound = triggerRoadCache.getTriggerRoad("I80");
        
        // verify null
        assertNull(triggerRoadFound);

        // cleanup
        triggerRoadCache.shutdown();
    }

}
