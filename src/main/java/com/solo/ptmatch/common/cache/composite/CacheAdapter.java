package com.solo.ptmatch.common.cache.composite;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import java.util.List;
import java.util.Map;

public interface CacheAdapter {

    <V> V get(CacheTarget cacheDef, String key);

    <V> Map<String, V> getAll(CacheTarget cacheDef, List<String> keys);

    <V> void put(CacheTarget cacheDef, String key, V value);

    <V> void putAll(CacheTarget cacheDef, Map<String, V> data);

    void evict(CacheTarget cacheDef, String key);
}
