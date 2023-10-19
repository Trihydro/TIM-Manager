package com.trihydro.library.helpers.caches;

import java.util.Properties;

public class JCSCacheConfig {
    private String jcsDefault;
    private String jcsDefaultCacheAttributes;
    private String jcsDefaultCacheAttributesMaxObjects;
    private String jcsDefaultCacheAttributesMemoryCacheName;
    private String jcsDefaultCacheAttributesUseMemoryShrinker;
    private String jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds;
    private String jcsDefaultCacheAttributesShrinkerIntervalSeconds;
    private String jcsDefaultCacheAttributesMaxSpoolPerRun;
    private String jcsDefaultElementAttributes;
    private String jcsDefaultElementAttributesIsEternal;
    private String jcsDefaultElementAttributesMaxLife;
    private String jcsDefaultElementAttributesIsSpool;
    private String jcsDefaultElementAttributesIsRemote;
    private String jcsDefaultElementAttributesIsLateral;
    
    public JCSCacheConfig() {
        jcsDefault = "";
        jcsDefaultCacheAttributes = "org.apache.commons.jcs.engine.CompositeCacheAttributes";
        jcsDefaultCacheAttributesMaxObjects = "1000";
        jcsDefaultCacheAttributesMemoryCacheName = "org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache";
        jcsDefaultCacheAttributesUseMemoryShrinker = "true";
        jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds = "3600"; // 1 hour
        jcsDefaultCacheAttributesShrinkerIntervalSeconds = "3600"; // 1 hour
        jcsDefaultCacheAttributesMaxSpoolPerRun = "500";
        jcsDefaultElementAttributes = "org.apache.commons.jcs.engine.ElementAttributes";
        jcsDefaultElementAttributesIsEternal = "false";
        jcsDefaultElementAttributesMaxLife = "3600"; // 1 hour
        jcsDefaultElementAttributesIsSpool = "true";
        jcsDefaultElementAttributesIsRemote = "false";
        jcsDefaultElementAttributesIsLateral = "false";
    }

    public String getJcsDefault() {
        return jcsDefault;
    }

    public void setJcsDefault(String jcsDefault) {
        this.jcsDefault = jcsDefault;
    }

    public String getJcsDefaultCacheAttributes() {
        return jcsDefaultCacheAttributes;
    }

    public void setJcsDefaultCacheAttributes(String jcsDefaultCacheAttributes) {
        this.jcsDefaultCacheAttributes = jcsDefaultCacheAttributes;
    }

    public String getJcsDefaultCacheAttributesMaxObjects() {
        return jcsDefaultCacheAttributesMaxObjects;
    }

    public void setJcsDefaultCacheAttributesMaxObjects(String jcsDefaultCacheAttributesMaxObjects) {
        this.jcsDefaultCacheAttributesMaxObjects = jcsDefaultCacheAttributesMaxObjects;
    }

    public String getJcsDefaultCacheAttributesMemoryCacheName() {
        return jcsDefaultCacheAttributesMemoryCacheName;
    }

    public void setJcsDefaultCacheAttributesMemoryCacheName(String jcsDefaultCacheAttributesMemoryCacheName) {
        this.jcsDefaultCacheAttributesMemoryCacheName = jcsDefaultCacheAttributesMemoryCacheName;
    }

    public String getJcsDefaultCacheAttributesUseMemoryShrinker() {
        return jcsDefaultCacheAttributesUseMemoryShrinker;
    }

    public void setJcsDefaultCacheAttributesUseMemoryShrinker(String jcsDefaultCacheAttributesUseMemoryShrinker) {
        this.jcsDefaultCacheAttributesUseMemoryShrinker = jcsDefaultCacheAttributesUseMemoryShrinker;
    }

    public String getJcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds() {
        return jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds;
    }

    public void setJcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds(
            String jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds) {
        this.jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds = jcsDefaultCacheAttributesMaxMemoryIdleTimeSeconds;
    }

    public String getJcsDefaultCacheAttributesShrinkerIntervalSeconds() {
        return jcsDefaultCacheAttributesShrinkerIntervalSeconds;
    }

    public void setJcsDefaultCacheAttributesShrinkerIntervalSeconds(
            String jcsDefaultCacheAttributesShrinkerIntervalSeconds) {
        this.jcsDefaultCacheAttributesShrinkerIntervalSeconds = jcsDefaultCacheAttributesShrinkerIntervalSeconds;
    }

    public String getJcsDefaultCacheAttributesMaxSpoolPerRun() {
        return jcsDefaultCacheAttributesMaxSpoolPerRun;
    }

    public void setJcsDefaultCacheAttributesMaxSpoolPerRun(String jcsDefaultCacheAttributesMaxSpoolPerRun) {
        this.jcsDefaultCacheAttributesMaxSpoolPerRun = jcsDefaultCacheAttributesMaxSpoolPerRun;
    }

    public String getJcsDefaultElementAttributes() {
        return jcsDefaultElementAttributes;
    }

    public void setJcsDefaultElementAttributes(String jcsDefaultElementAttributes) {
        this.jcsDefaultElementAttributes = jcsDefaultElementAttributes;
    }

    public String getJcsDefaultElementAttributesIsEternal() {
        return jcsDefaultElementAttributesIsEternal;
    }

    public void setJcsDefaultElementAttributesIsEternal(String jcsDefaultElementAttributesIsEternal) {
        this.jcsDefaultElementAttributesIsEternal = jcsDefaultElementAttributesIsEternal;
    }

    public String getJcsDefaultElementAttributesMaxLife() {
        return jcsDefaultElementAttributesMaxLife;
    }

    public void setJcsDefaultElementAttributesMaxLife(String jcsDefaultElementAttributesMaxLife) {
        this.jcsDefaultElementAttributesMaxLife = jcsDefaultElementAttributesMaxLife;
    }

    public String getJcsDefaultElementAttributesIsSpool() {
        return jcsDefaultElementAttributesIsSpool;
    }

    public void setJcsDefaultElementAttributesIsSpool(String jcsDefaultElementAttributesIsSpool) {
        this.jcsDefaultElementAttributesIsSpool = jcsDefaultElementAttributesIsSpool;
    }

    public String getJcsDefaultElementAttributesIsRemote() {
        return jcsDefaultElementAttributesIsRemote;
    }

    public void setJcsDefaultElementAttributesIsRemote(String jcsDefaultElementAttributesIsRemote) {
        this.jcsDefaultElementAttributesIsRemote = jcsDefaultElementAttributesIsRemote;
    }

    public String getJcsDefaultElementAttributesIsLateral() {
        return jcsDefaultElementAttributesIsLateral;
    }

    public void setJcsDefaultElementAttributesIsLateral(String jcsDefaultElementAttributesIsLateral) {
        this.jcsDefaultElementAttributesIsLateral = jcsDefaultElementAttributesIsLateral;
    }

    public Properties getAsProperties() {
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
        return props;
    } 
}
