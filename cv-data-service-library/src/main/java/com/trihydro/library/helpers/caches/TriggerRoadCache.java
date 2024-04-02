package com.trihydro.library.helpers.caches;

import java.util.List;
import java.util.Properties;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.JCSCacheProps;
import com.trihydro.library.model.TriggerRoad;

public class TriggerRoadCache {
    private Utility utility;
    private JCSCacheProps config;
    private CacheAccess<String, List<Integer>> cache;

    public TriggerRoadCache(Utility _utility, JCSCacheProps _config) throws CacheException {
        utility = _utility;
        config = _config;
        cache = createCache();
    }

    public List<Integer> getSegmentIdsAssociatedWithTriggerRoad(String roadCode) {
        return cache.get(roadCode);
    }

    public boolean isCached(String roadCode) {
        return cache.get(roadCode) != null;
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
        Properties props = convertToProperties(config);
        ccm.configure(props);
        CacheAccess<String, List<Integer>> cache = JCS.getInstance("default");
        return cache;
    }

    private void printConfig() {
        utility.logWithDate("[TriggerRoadCache] Cache Configuration:");
        utility.logWithDate("[TriggerRoadCache] - jcs.default: " + config.getJcsDefault());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes: " + config.getCacheAttributes());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MaxObjects: " + config.getMaxObjects());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MemoryCacheName: " + config.getMemoryCacheName());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.UseMemoryShrinker: " + config.getUseMemoryShrinker());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds: " + config.getMaxMemoryIdleTimeSeconds());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.ShrinkerIntervalSeconds: " + config.getShrinkerIntervalSeconds());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.cacheattributes.MaxSpoolPerRun: " + config.getMaxSpoolPerRun());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes: " + config.getElementAttributes());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsEternal: " + config.getIsEternal());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.MaxLifeSeconds: " + config.getMaxLife());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsSpool: " + config.getIsSpool());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsRemote: " + config.getIsRemote());
        utility.logWithDate("[TriggerRoadCache] - jcs.default.elementattributes.IsLateral: " + config.getIsLateral());
    }

    private Properties convertToProperties(JCSCacheProps config) {
        Properties props = new Properties();
        props.setProperty("jcs.default", config.getJcsDefault());
        props.setProperty("jcs.default.cacheattributes", config.getCacheAttributes());
        props.setProperty("jcs.default.cacheattributes.MaxObjects", config.getMaxObjects());
        props.setProperty("jcs.default.cacheattributes.MemoryCacheName", config.getMemoryCacheName());
        props.setProperty("jcs.default.cacheattributes.UseMemoryShrinker", config.getUseMemoryShrinker());
        props.setProperty("jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds", config.getMaxMemoryIdleTimeSeconds());
        props.setProperty("jcs.default.cacheattributes.ShrinkerIntervalSeconds", config.getShrinkerIntervalSeconds());
        props.setProperty("jcs.default.cacheattributes.MaxSpoolPerRun", config.getMaxSpoolPerRun());
        props.setProperty("jcs.default.elementattributes", config.getElementAttributes());
        props.setProperty("jcs.default.elementattributes.IsEternal", config.getIsEternal());
        props.setProperty("jcs.default.elementattributes.MaxLifeSeconds", config.getMaxLife());
        props.setProperty("jcs.default.elementattributes.IsSpool", config.getIsSpool());
        props.setProperty("jcs.default.elementattributes.IsRemote", config.getIsRemote());
        props.setProperty("jcs.default.elementattributes.IsLateral", config.getIsLateral());
        return props;
    }
}
