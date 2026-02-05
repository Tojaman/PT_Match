package com.solo.ptmatch.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.solo.ptmatch.common.cache.enums.CacheTarget;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@EnableCaching
@Configuration
public class CacheConfig {

    private static final double TTL_JITTER_FRACTION = 0.10d;

    // 로컬(Caffeine) 캐시 매니저에 캐시 생성
    @Bean
    @Primary
    public CacheManager simpleCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = Arrays.stream(CacheTarget.values())
                .map(target -> new CaffeineCache(target.name(), buildLocalCache(target)))
                .toList();
        cacheManager.setCaches(caches); // 캐시 매니저에 주입
        return cacheManager;
    }

    private Cache<Object, Object> buildLocalCache(CacheTarget def) {
        return Caffeine.newBuilder()
                .maximumSize(def.getMaxEntries())
                .expireAfter(buildJitteredExpiry(Duration.ofSeconds(def.getTtlSeconds())))
                .build();
    }

    private Expiry<Object, Object> buildJitteredExpiry(Duration baseTtl) {
        long baseNanos = baseTtl.toNanos();
        long jitterNanos = (long) (baseNanos * TTL_JITTER_FRACTION);
        return new Expiry<>() {
            @Override
            public long expireAfterCreate(Object key, Object value, long currentTime) {
                if (jitterNanos <= 0) {
                    return baseNanos;
                }
                long delta = ThreadLocalRandom.current().nextLong(-jitterNanos, jitterNanos + 1);
                long ttl = baseNanos + delta;
                return Math.max(1L, ttl);
            }

            @Override
            public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                return currentDuration;
            }
        };
    }
}
