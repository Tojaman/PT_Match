package com.solo.ptmatch.common.cache.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    @SneakyThrows
    public void publish(CacheTarget cacheTarget, String key) {
        CacheEvictionMessage msg = new CacheEvictionMessage(cacheTarget, key);
        String message = objectMapper.writeValueAsString(msg);
        redisTemplate.convertAndSend(topic.getTopic(), message);
        log.info("publish 완료");
    }
}
