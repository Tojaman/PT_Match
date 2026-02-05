package com.solo.ptmatch.common.cache.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.cache.enums.CacheTarget;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic topic = new ChannelTopic("cache:evict");

    private final ObjectMapper objectMapper;

    public void publish(CacheTarget cacheTarget, String key) {
        try {
            CacheEvictionMessage msg = new CacheEvictionMessage(cacheTarget, key);
            String message = objectMapper.writeValueAsString(msg);
            redisTemplate.convertAndSend(topic.getTopic(), message);
            log.info("publish 완료");
        } catch (Exception e) {
            log.warn("Redis Pub/Sub publish 실패: cacheTarget={}, key={}", cacheTarget, key, e);
        }
    }
}
