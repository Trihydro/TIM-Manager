package com.trihydro.library.helpers.caches;

import java.util.List;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.TriggerRoad;

public class TriggerRoadCache {
    private Utility utility = new Utility();
    private CacheAccess<String, List<Integer>> cache;
    private JCSCacheConfig config = new JCSCacheConfig();

    public TriggerRoadCache(Utility _utility) throws CacheException {
        utility = _utility;
        cache = createCache();
    }

    public List<Integer> getSegmentIdsAssociatedWithTriggerRoad(String roadCode) throws NotCachedException {
        List<Integer> segmentIds = cache.get(roadCode);
        if (segmentIds == null) {
            throw new NotCachedException("Trigger road " + roadCode + " not found in cache");
        }
        return segmentIds;
    }

    public void updateCache(String roadCode, TriggerRoad triggerRoad) {
        cache.put(roadCode, triggerRoad.getCountyRoadSegmentIds());
    }

    public void clear() {
        cache.clear();
    }

    public void shutdown() {
        utility.logWithDate("[TriggerRoadCache] \n====================\nShutting down cache...\n====================\n");
        cache.getCacheControl().dispose();
        utility.logWithDate("[TriggerRoadCache] \n====================\nCache shut down\n====================\n");
    }

    private CacheAccess<String, List<Integer>> createCache() throws CacheException {
        utility.logWithDate("[TriggerRoadCache] \n====================\nCreating cache...\n====================\n");
        CacheAccess<String, List<Integer>> cache = createCacheSimple();
        utility.logWithDate("[TriggerRoadCache] \n====================\nCache created\n====================\n");
        return cache;
    }

    private CacheAccess<String, List<Integer>> createCacheSimple() throws CacheException {
        CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance();
        printConfig();
        ccm.configure(config.getAsProperties());
        CacheAccess<String, List<Integer>> cache = JCS.getInstance("default");
        return cache;
    }

    private void printConfig() {
        utility.logWithDate("[TriggerRoadCache] Cache Configuration:");
        utility.logWithDate("[TriggerRoadCache] - jcs.default: " + config.getJcsDefault());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes: " + config.getJcsDefaultCacheAttributes());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MaxObjects: " + config.getJcsDefaultCacheAttributesMaxObjects());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MemoryCacheName: " + config.getJcsDefaultCacheAttributesMemoryCacheName());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.UseMemoryShrinker: " + config.getJcsDefaultCacheAttributesUseMemoryShrinker());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds: " + config.getJcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.ShrinkerIntervalSeconds: " + config.getJcsDefaultCacheAttributesShrinkerIntervalSeconds());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MaxSpoolPerRun: " + config.getJcsDefaultCacheAttributesMaxSpoolPerRun());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes: " + config.getJcsDefaultElementAttributes());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsEternal: " + config.getJcsDefaultElementAttributesIsEternal());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.MaxLifeSeconds: " + config.getJcsDefaultElementAttributesMaxLife());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsSpool: " + config.getJcsDefaultElementAttributesIsSpool());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsRemote: " + config.getJcsDefaultElementAttributesIsRemote());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsLateral: " + config.getJcsDefaultElementAttributesIsLateral());

    }

    public class NotCachedException extends Exception {
        private static final long serialVersionUID = 1L;
        public NotCachedException(String message) {
            super(message);
        }
    }
}
