package com.trihydro.library.model;

public interface JCSCacheProps {
    String getJcsDefault();
    void setJcsDefault(String jcsDefault);

    // cache attributes
    String getCacheAttributes();
    void setCacheAttributes(String cacheAttributes);

    String getMaxObjects();
    void setMaxObjects(String maxObjects);

    String getMemoryCacheName();
    void setMemoryCacheName(String memoryCacheName);

    String getUseMemoryShrinker();
    void setUseMemoryShrinker(String useMemoryShrinker);

    String getMaxMemoryIdleTimeSeconds();
    void setMaxMemoryIdleTimeSeconds(String maxMemoryIdleTimeSeconds);

    String getShrinkerIntervalSeconds();
    void setShrinkerIntervalSeconds(String shrinkerIntervalSeconds);

    String getMaxSpoolPerRun();
    void setMaxSpoolPerRun(String maxSpoolPerRun);

    // element attributes
    String getElementAttributes();
    void setElementAttributes(String elementAttributes);

    String getIsEternal();
    void setIsEternal(String isEternal);

    String getMaxLife();
    void setMaxLife(String maxLife);

    String getIsSpool();
    void setIsSpool(String isSpool);

    String getIsRemote();
    void setIsRemote(String isRemote);

    String getIsLateral();
    void setIsLateral(String isLateral);
}