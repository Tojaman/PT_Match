package com.solo.ptmatch.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

@EnableCaching
@Configuration
public class CacheConfig {

    // 로컬(Caffeine) 캐시 매니저에 캐시 생성
    @Bean
    @Primary
    public CacheManager simpleCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = Arrays.stream(CacheTarget.values())
                .map(target -> new CaffeineCache("hahahaha", buildLocalCache(target)))
                .toList();
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    // 고정 TTL 사용 - 같은 시점에 캐시된 cell들이 동시에 만료되도록 함
    // Cache Stampede 방지는 TrainerMapProvider의 뮤텍스 락이 담당
    private Cache<Object, Object> buildLocalCache(CacheTarget def) {
        return Caffeine.newBuilder()
                .maximumSize(def.getMaxEntries())
                .expireAfterWrite(Duration.ofSeconds(def.getTtlSeconds()))
                .build();
    }
}
