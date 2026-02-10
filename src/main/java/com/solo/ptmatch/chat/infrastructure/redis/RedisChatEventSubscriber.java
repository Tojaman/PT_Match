package com.solo.ptmatch.chat.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 채널 "chat:room:*" 을 구독하여
 * 다른 인스턴스에서 발행된 채팅 이벤트를 수신하고
 * 로컬 WebSocket 세션에 브로드캐스트합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatEventSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            // 채널 형식: chat:room:{roomId}
            String roomId = extractRoomId(channel);

            log.debug("Redis 채팅 이벤트 수신 - channel: {}, roomId: {}", channel, roomId);

            // 해당 방을 구독 중인 로컬 WebSocket 세션에 메시지 전달
            messagingTemplate.convertAndSend(
                    "/topic/chat/rooms/" + roomId,
                    objectMapper.readTree(body));
        } catch (Exception e) {
            log.error("Redis 채팅 이벤트 처리 실패", e);
        }
    }

    private String extractRoomId(String channel) {
        // "chat:room:123" -> "123"
        return channel.substring(channel.lastIndexOf(':') + 1);
    }
}
