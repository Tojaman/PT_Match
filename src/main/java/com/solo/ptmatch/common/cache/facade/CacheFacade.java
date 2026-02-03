package com.solo.ptmatch.common.cache.facade;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheFacade {

    private final CacheManager simpleCacheManager;
//    private final RedisCacheManager redisCacheManager;

    public <V> V getOrPut(CacheTarget cacheTarget, String key, Supplier<V> loader) {
        V cached = get(cacheTarget, key);
        if (cached != null) return cached;

        V loaded = loader.get();
        if (loaded != null) put(cacheTarget, key, loaded);
        return loaded;
    }

    public <V> V get(CacheTarget cacheTarget, String key) {
        Cache cache = getByCacheTarget(cacheTarget);
        if (cache == null) {
            return null;
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        V value = (V) wrapper.get();
        return value;
    }

    public <V> void put(CacheTarget cacheTarget, String key, V value) {
        Cache cache = getByCacheTarget(cacheTarget);
        if (cache != null) cache.put(key, value);
    }

    public void evict(CacheTarget cacheTarget, String key) {
        Cache cache = getByCacheTarget(cacheTarget);
        if (cache != null) cache.evict(key);
    }

    private Cache getByCacheTarget(CacheTarget cacheTarget) {
        return switch (cacheTarget.getType()) {
//            case GLOBAL -> redisCacheManager.getCache(cacheTarget.getName());
            case LOCAL -> simpleCacheManager.getCache(cacheTarget.getName());
        };
    }
}
