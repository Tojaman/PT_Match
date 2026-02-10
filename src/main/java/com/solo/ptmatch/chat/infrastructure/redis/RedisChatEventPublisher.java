package com.solo.ptmatch.chat.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.chat.presentation.response.ChatEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 채팅 이벤트를 Redis Pub/Sub 채널 "chat:room:{roomId}" 로 발행합니다.
 * 멀티 인스턴스 환경에서 다른 인스턴스의 RedisChatEventSubscriber가 수신합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatEventPublisher {

    private static final String CHANNEL_PREFIX = "chat:room:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(ChatEventResponse event) {
        try {
            String channel = CHANNEL_PREFIX + event.roomId();
            String payload = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(channel, payload);
            log.debug("채팅 이벤트 발행 - channel: {}, type: {}", channel, event.type());
        } catch (Exception e) {
            log.error("채팅 이벤트 발행 실패 - roomId: {}", event.roomId(), e);
        }
    }
}
