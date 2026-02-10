package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "읽음 상태 갱신 결과")
public record ChatReadUpdateResponse(
        @Schema(description = "채팅방 ID", example = "12") Long roomId,

        @Schema(description = "사용자 ID", example = "1") Long userId,

        @Schema(description = "마지막으로 읽은 메시지 ID", example = "201") Long lastReadMessageId,

        @Schema(description = "읽음 시각", example = "2025-09-16T11:05:00") LocalDateTime lastReadAt) {
}
