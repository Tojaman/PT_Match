package com.solo.ptmatch.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.solo.ptmatch.common.cache.enums.CacheType;
import com.solo.ptmatch.common.cache.enums.CacheTarget;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@EnableCaching
@Configuration
public class CacheConfig {

    // 로컬(Caffeine) 캐시 매니저에 캐시 생성
    @Bean
    @Primary
    public CacheManager simpleCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = Arrays.stream(CacheTarget.values())
//                .filter(def -> def.getType() != CacheType.GLOBAL) // LOCAL, LAYERED 캐시만 생성
                .map(def -> new CaffeineCache(def.getName(), buildLocalCache(def)))
                .toList();
        cacheManager.setCaches(caches); // 캐시 매니저에 주입
        return cacheManager;
    }

    private Cache<Object, Object> buildLocalCache(CacheTarget def) {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(def.getTtlSeconds())).build();
    }
}
