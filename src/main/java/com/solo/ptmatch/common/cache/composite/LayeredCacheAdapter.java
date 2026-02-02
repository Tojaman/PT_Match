package com.solo.ptmatch.common.cache.composite;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class LayeredCacheAdapter implements CacheAdapter {

    private final LocalCacheAdapter localCacheManager;
    private final GlobalCacheAdapter globalCacheManager;

    @Override
    public <V> V get(CacheTarget cacheDef, String key) {
        V local = localCacheManager.get(cacheDef, key);
        if (local != null)
            return local;

        V global = globalCacheManager.get(cacheDef, key);
        if (global != null)
            localCacheManager.put(cacheDef, key, global);

        return global;
    }

    @Override
    public <V> void put(CacheTarget cacheDef, String key, V value) {
        localCacheManager.put(cacheDef, key, value);
        globalCacheManager.put(cacheDef, key, value);
    }

    @Override
    public void evict(CacheTarget cacheDef, String key) {
        localCacheManager.evict(cacheDef, key);
        globalCacheManager.evict(cacheDef, key);
    }

    @Override
    public <V> Map<String, V> getAll(CacheTarget cacheDef, List<String> keys) {

        Map<String, V> localHits = localCacheManager.getAll(cacheDef, keys);
        if (localHits.size() == keys.size()) {
            return localHits;
        }

        List<String> misses = new java.util.ArrayList<>();
        for (String key : keys) {
            if (!localHits.containsKey(key)) {
                misses.add(key);
            }
        }

        if (misses.isEmpty()) {
            return localHits;
        }

        Map<String, V> globalHits = globalCacheManager.getAll(cacheDef, misses);
        if (!globalHits.isEmpty()) {
            localCacheManager.putAll(cacheDef, globalHits);
            Map<String, V> merged = new java.util.HashMap<>(localHits);
            merged.putAll(globalHits);
            return merged;
        }
        return localHits;
    }

    @Override
    public <V> void putAll(CacheTarget cacheDef, Map<String, V> data) {
        localCacheManager.putAll(cacheDef, data);
        globalCacheManager.putAll(cacheDef, data);
    }

}
