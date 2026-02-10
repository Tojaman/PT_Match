package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "채팅 이벤트 (Redis Pub/Sub 및 WebSocket 브로드캐스트 페이로드)")
public record ChatEventResponse(
        @Schema(description = "이벤트 타입", example = "MESSAGE") String type,

        @Schema(description = "채팅방 ID", example = "12") Long roomId,

        @Schema(description = "메시지 ID (MESSAGE 타입일 때)") Long messageId,

        @Schema(description = "발신자/읽은 사용자 ID") Long userId,

        @Schema(description = "메시지 내용 (MESSAGE 타입일 때)") String content,

        @Schema(description = "마지막 읽은 메시지 ID (READ 타입일 때)") Long lastReadMessageId,

        @Schema(description = "이벤트 시각") LocalDateTime timestamp) {

    public static ChatEventResponse messageCreated(
            Long roomId, Long messageId, Long senderId, String content, LocalDateTime sentAt) {
        return new ChatEventResponse("MESSAGE", roomId, messageId, senderId, content, null, sentAt);
    }

    public static ChatEventResponse readUpdated(
            Long roomId, Long userId, Long lastReadMessageId, LocalDateTime readAt) {
        return new ChatEventResponse("READ", roomId, null, userId, null, lastReadMessageId, readAt);
    }
}
