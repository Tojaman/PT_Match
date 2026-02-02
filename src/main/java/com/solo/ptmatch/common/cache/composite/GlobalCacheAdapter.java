package com.solo.ptmatch.common.cache.composite;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class GlobalCacheAdapter implements CacheAdapter {

    private final RedisTemplate<String, Object> cacheRedisTemplate;

    @SuppressWarnings("unchecked") // put에서 검증했으므로 안전함
    @Override
    public <V> V get(CacheTarget cacheDef, String key) {
        String redisKey = buildKey(cacheDef, key);
        return (V) cacheRedisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public <V> void put(CacheTarget cacheDef, String key, V value) {
        String redisKey = buildKey(cacheDef, key);
        Duration ttl = Duration.ofSeconds(cacheDef.getTtlSeconds());
        cacheRedisTemplate.opsForValue().set(redisKey, value, ttl);
    }

    @Override
    public void evict(CacheTarget cacheDef, String key) {
        cacheRedisTemplate.delete(buildKey(cacheDef, key));
    }

    @SuppressWarnings("unchecked") // put에서 검증했으므로 안전함
    @Override
    public <V> Map<String, V> getAll(CacheTarget cacheDef, List<String> keys) {
        List<String> redisKeys = keys.stream()
                .map(key -> buildKey(cacheDef, key))
                .toList();
        List<Object> values = cacheRedisTemplate.opsForValue().multiGet(redisKeys);

        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, V> result = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            if (values.get(i) != null) {
                V value = (V) values.get(i);                                                                                                                                                                                  
                result.put(keys.get(i), value);
            }
        }
        return result;
    }

    @Override
    public <V> void putAll(CacheTarget cacheDef, Map<String, V> data) {
        Duration ttl = Duration.ofSeconds(cacheDef.getTtlSeconds());

        cacheRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            data.forEach((key, value) -> {
                String realKey = buildKey(cacheDef, key);
                cacheRedisTemplate.opsForValue().set(realKey, value, ttl);
            });
            return null;
        });
    }

    private String buildKey(CacheTarget cacheDef, String key) {
        return cacheDef.getName() + "::" + key;
    }
}
