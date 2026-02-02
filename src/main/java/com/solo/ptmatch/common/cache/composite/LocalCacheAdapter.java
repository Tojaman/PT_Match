package com.solo.ptmatch.common.cache.composite;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class LocalCacheAdapter implements CacheAdapter {

    private final CacheManager cacheManager;

    @SuppressWarnings("unchecked") // put에서 검증했으므로 안전함
    @Override
    public <V> V get(CacheTarget cacheDef, String key) {
        if (cacheDef == null || key == null) {
            return null;
        }
        Cache cache = cacheManager.getCache(cacheDef.getName());
        
        V value = (V) (cache != null ? cache.get(key).get() : null);
        return value;
    }

    @Override
    public <V> void put(CacheTarget cacheDef, String key, V value) {
        Cache cache = cacheManager.getCache(cacheDef.getName());
        if (cache != null) cache.put(key, value);
    }

    @Override
    public void evict(CacheTarget cacheDef, String key) {
        Cache cache = cacheManager.getCache(cacheDef.getName());
        if (cache != null) cache.evict(key);
    }

    @SuppressWarnings("unchecked") // put에서 검증했으므로 안전함
    @Override
    public <V> Map<String, V> getAll(CacheTarget cacheDef, List<String> keys) {
        Cache cache = cacheManager.getCache(cacheDef.getName());
        if (cache == null) return Map.of();

        var nativeCache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();                                                                                                                                                                           
        return (Map<String, V>) (Map) new HashMap<>(nativeCache.getAllPresent(keys));
    }

    @Override
    public <V> void putAll(CacheTarget cacheDef, Map<String, V> data) {
        Cache cache = cacheManager.getCache(cacheDef.getName());
        if (cache == null) return;

        Map<String, V> casted = new HashMap<>(data.size());
        for (Map.Entry<String, V> entry : data.entrySet()) {
            cache.put(entry.getKey(), entry.getValue());
            casted.put(entry.getKey(), entry.getValue());
        }
    }
}
