package com.trihydro.library.helpers.caches;

import java.util.List;
import java.util.Properties;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.TriggerRoad;

public class TriggerRoadCache {
    private Utility utility = new Utility();
    private CacheAccess<String, List<Integer>> cache;

    // cache attributes
    private static final String jcsDefault = "";
    private static final String jcsDefaultCacheAttributes = "org.apache.commons.jcs.engine.CompositeCacheAttributes";
    private static final String jcsDefaultCacheAttributesMaxObjects = "1000";
    private static final String jcsDefaultCacheAttributesMemoryCacheName = "org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache";
    private static final String jcsDefaultCacheAttributesUseMemoryShrinker = "true";
    private static final String jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds = "3600"; // 1 hour
    private static final String jcsDefaultCacheAttributesShrinkerIntervalSeconds = "3600"; // 1 hour
    private static final String jcsDefaultCacheAttributesMaxSpoolPerRun = "500";

    // element attributes
    private static final String jcsDefaultElementAttributes = "org.apache.commons.jcs.engine.ElementAttributes";
    private static final String jcsDefaultElementAttributesIsEternal = "false";
    private static final String jcsDefaultElementAttributesMaxLife = "3600"; // 1 hour
    private static final String jcsDefaultElementAttributesIsSpool = "true";
    private static final String jcsDefaultElementAttributesIsRemote = "false";
    private static final String jcsDefaultElementAttributesIsLateral = "false";

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
        utility.logWithDate("[TriggerCache] \n====================\nShutting down cache...\n====================\n");
        cache.getCacheControl().dispose();
        utility.logWithDate("[TriggerCache] \n====================\nCache shut down\n====================\n");
    }

    private CacheAccess<String, List<Integer>> createCache() throws CacheException {
        utility.logWithDate("[TriggerCache] \n====================\nCreating cache...\n====================\n");
        CacheAccess<String, List<Integer>> cache = createCacheSimple();
        utility.logWithDate("[TriggerCache] \n====================\nCache created\n====================\n");
        return cache;
    }

    private CacheAccess<String, List<Integer>> createCacheSimple() throws CacheException {
        CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance();
        Properties props = new Properties();
        
        props.put("jcs.default", jcsDefault);
        props.put("jcs.default.cacheattributes", jcsDefaultCacheAttributes);
        props.put("jcs.default.cacheattributes.MaxObjects", jcsDefaultCacheAttributesMaxObjects);
        props.put("jcs.default.cacheattributes.MemoryCacheName", jcsDefaultCacheAttributesMemoryCacheName);
        
        props.put("jcs.default.cacheattributes.UseMemoryShrinker", jcsDefaultCacheAttributesUseMemoryShrinker);
        props.put("jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds", jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds);
        props.put("jcs.default.cacheattributes.ShrinkerIntervalSeconds", jcsDefaultCacheAttributesShrinkerIntervalSeconds);
        props.put("jcs.default.cacheattributes.MaxSpoolPerRun", jcsDefaultCacheAttributesMaxSpoolPerRun);
        props.put("jcs.default.elementattributes", jcsDefaultElementAttributes);
        props.put("jcs.default.elementattributes.IsEternal", jcsDefaultElementAttributesIsEternal);
        props.put("jcs.default.elementattributes.MaxLife", jcsDefaultElementAttributesMaxLife);
        props.put("jcs.default.elementattributes.IsSpool", jcsDefaultElementAttributesIsSpool);
        props.put("jcs.default.elementattributes.IsRemote", jcsDefaultElementAttributesIsRemote);
        props.put("jcs.default.elementattributes.IsLateral", jcsDefaultElementAttributesIsLateral);

        utility.logWithDate("[TriggerCache] Cache Configuration:");
        utility.logWithDate("[TriggerCache] - jcs.default: " + props.getProperty("jcs.default"));
        utility.logWithDate("[TriggerCache] - jcs.default.cacheattributes: " + props.getProperty("jcs.default.cacheattributes"));
        utility.logWithDate("[TriggerCache] - jcs.default.cacheattributes.MaxObjects: " + props.getProperty("jcs.default.cacheattributes.MaxObjects"));
        utility.logWithDate("[TriggerCache] - jcs.default.cacheattributes.MemoryCacheName: " + props.getProperty("jcs.default.cacheattributes.MemoryCacheName"));
        utility.logWithDate("[TriggerCache] - jcs.default.cacheattributes.UseMemoryShrinker: " + props.getProperty("jcs.default.cacheattributes.UseMemoryShrinker"));
        utility.logWithDate("[TriggerCache] - jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds: " + props.getProperty("jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds"));
        utility.logWithDate("[TriggerCache] - jcs.default.cacheattributes.ShrinkerIntervalSeconds: " + props.getProperty("jcs.default.cacheattributes.ShrinkerIntervalSeconds"));
        utility.logWithDate("[TriggerCache] - jcs.default.cacheattributes.MaxSpoolPerRun: " + props.getProperty("jcs.default.cacheattributes.MaxSpoolPerRun"));
        utility.logWithDate("[TriggerCache] - jcs.default.elementattributes: " + props.getProperty("jcs.default.elementattributes"));
        utility.logWithDate("[TriggerCache] - jcs.default.elementattributes.IsEternal: " + props.getProperty("jcs.default.elementattributes.IsEternal"));
        utility.logWithDate("[TriggerCache] - jcs.default.elementattributes.MaxLife: " + props.getProperty("jcs.default.elementattributes.MaxLife"));
        utility.logWithDate("[TriggerCache] - jcs.default.elementattributes.IsSpool: " + props.getProperty("jcs.default.elementattributes.IsSpool"));
        utility.logWithDate("[TriggerCache] - jcs.default.elementattributes.IsRemote: " + props.getProperty("jcs.default.elementattributes.IsRemote"));
        utility.logWithDate("[TriggerCache] - jcs.default.elementattributes.IsLateral: " + props.getProperty("jcs.default.elementattributes.IsLateral"));
        
        ccm.configure(props);
        CacheAccess<String, List<Integer>> cache = JCS.getInstance("default");
        return cache;
    }

    public class NotCachedException extends Exception {
        private static final long serialVersionUID = 1L;
        public NotCachedException(String message) {
            super(message);
        }
    }
}
