package com.solo.ptmatch.common.cache.facade;

import com.solo.ptmatch.common.cache.composite.CacheAdapter;
import com.solo.ptmatch.common.cache.composite.GlobalCacheAdapter;
import com.solo.ptmatch.common.cache.composite.LayeredCacheAdapter;
import com.solo.ptmatch.common.cache.composite.LocalCacheAdapter;
import com.solo.ptmatch.common.cache.enums.CacheType;
import com.solo.ptmatch.common.cache.enums.CacheTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheRouter {

    private final LocalCacheAdapter localCacheManager;
    private final GlobalCacheAdapter globalCacheManager;
    private final LayeredCacheAdapter layeredCacheManager;

    public <V> V get(CacheTarget cacheDef, String key) {
        CacheAdapter manager = resolveProvider(cacheDef);
        return manager.get(cacheDef, key);
    }

    public <V> void put(CacheTarget cacheDef, String key, V value) {
        CacheAdapter manager = resolveProvider(cacheDef);
        manager.put(cacheDef, key, value);
    }

    public void evict(CacheTarget cacheDef, String key) {
        CacheAdapter manager = resolveProvider(cacheDef);
        manager.evict(cacheDef, key);
    }

    public <V> Map<String, V> getAll(CacheTarget cacheDef, List<String> keys) {
        CacheAdapter manager = resolveProvider(cacheDef);
        return manager.getAll(cacheDef, keys);
    }

    public <V> void putAll(CacheTarget cacheDef, Map<String, V> data) {
        CacheAdapter manager = resolveProvider(cacheDef);
        manager.putAll(cacheDef, data);
    }

    private CacheAdapter resolveProvider(CacheTarget cacheDef) {
        CacheType type = cacheDef != null ? cacheDef.getType() : CacheType.LOCAL;
        return switch (type) {
            case GLOBAL -> globalCacheManager;
            case LAYERED -> layeredCacheManager;
            case LOCAL -> localCacheManager;
        };
    }
}
