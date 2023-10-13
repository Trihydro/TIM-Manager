package com.trihydro.library.helpers.caches;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.TriggerRoad;

public class TriggerRoadCache {
    private Utility utility = new Utility();
    private CacheAccess<String, Serializable> cache;

    // cache attributes
    private static final String jcsDefault = "";
    private static final String jcsDefaultCacheAttributes = "org.apache.commons.jcs.engine.CompositeCacheAttributes";
    private static final String jcsDefaultCacheAttributesMaxObjects = "1000";
    private static final String jcsDefaultCacheAttributesMemoryCacheName = "org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache";
    private static final String jcsDefaultCacheAttributesUseMemoryShrinker = "true";
    private static final String jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds = "60";
    private static final String jcsDefaultCacheAttributesShrinkerIntervalSeconds = "60";
    private static final String jcsDefaultCacheAttributesMaxSpoolPerRun = "500";

    // element attributes
    private static final String jcsDefaultElementAttributes = "org.apache.commons.jcs.engine.ElementAttributes";
    private static final String jcsDefaultElementAttributesIsEternal = "false";
    private static final String jcsDefaultElementAttributesMaxLife = "60";
    private static final String jcsDefaultElementAttributesIsSpool = "true";
    private static final String jcsDefaultElementAttributesIsRemote = "false";
    private static final String jcsDefaultElementAttributesIsLateral = "false";

    public TriggerRoadCache(Utility _utility) throws CacheException {
        utility = _utility;
        cache = createCache();
    }

    public TriggerRoad getTriggerRoad(String roadCode) {
        return (TriggerRoad) cache.get(roadCode);
    }

    public void updateCache(String roadCode, TriggerRoad triggerRoad) {
        cache.put(roadCode, triggerRoad);
    }

    public void clear() {
        cache.clear();
    }

    public void shutdown() {
        log("\n====================\nShutting down cache...\n====================\n");
        cache.getCacheControl().dispose();
        log("\n====================\nCache shut down\n====================\n");
    }

    private CacheAccess<String, Serializable> createCache() throws CacheException {
        log("\n====================\nCreating cache...\n====================\n");
        CacheAccess<String, Serializable> cache = createCacheSimple();
        log("\n====================\nCache created\n====================\n");
        return cache;
    }

    private CacheAccess<String, Serializable> createCacheSimple() throws CacheException {
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

        log("Cache Configuration:");
        log("- jcs.default: " + props.getProperty("jcs.default"));
        log("- jcs.default.cacheattributes: " + props.getProperty("jcs.default.cacheattributes"));
        log("- jcs.default.cacheattributes.MaxObjects: " + props.getProperty("jcs.default.cacheattributes.MaxObjects"));
        log("- jcs.default.cacheattributes.MemoryCacheName: " + props.getProperty("jcs.default.cacheattributes.MemoryCacheName"));
        log("- jcs.default.cacheattributes.UseMemoryShrinker: " + props.getProperty("jcs.default.cacheattributes.UseMemoryShrinker"));
        log("- jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds: " + props.getProperty("jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds"));
        log("- jcs.default.cacheattributes.ShrinkerIntervalSeconds: " + props.getProperty("jcs.default.cacheattributes.ShrinkerIntervalSeconds"));
        log("- jcs.default.cacheattributes.MaxSpoolPerRun: " + props.getProperty("jcs.default.cacheattributes.MaxSpoolPerRun"));
        log("- jcs.default.elementattributes: " + props.getProperty("jcs.default.elementattributes"));
        log("- jcs.default.elementattributes.IsEternal: " + props.getProperty("jcs.default.elementattributes.IsEternal"));
        log("- jcs.default.elementattributes.MaxLife: " + props.getProperty("jcs.default.elementattributes.MaxLife"));
        log("- jcs.default.elementattributes.IsSpool: " + props.getProperty("jcs.default.elementattributes.IsSpool"));
        log("- jcs.default.elementattributes.IsRemote: " + props.getProperty("jcs.default.elementattributes.IsRemote"));
        log("- jcs.default.elementattributes.IsLateral: " + props.getProperty("jcs.default.elementattributes.IsLateral"));
        
        ccm.configure(props);
        CacheAccess<String, Serializable> cache = JCS.getInstance("default");
        return cache;
    }

    private void log(String message) {
        utility.logWithDate("[TriggerCache] " + message);
    }
}
