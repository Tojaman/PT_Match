package com.solo.ptmatch.common.cache.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.cache.composite.LocalCacheAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final LocalCacheAdapter localCacheAdapter;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            CacheEvictionMessage msg = objectMapper.readValue(message.getBody(), CacheEvictionMessage.class);
            localCacheAdapter.evict(msg.getCacheTarget(), msg.getKey());
            log.info("로컬 캐시 삭제됨: {} key: {}", msg.getCacheTarget(), msg.getKey());
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패", e);
        }
    }
}
