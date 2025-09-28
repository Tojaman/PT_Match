package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 전송 결과")
public record ChatMessageSendResponse(
    @Schema(description = "메시지 ID", example = "201")
    Long messageId,

    @Schema(description = "채팅방 ID", example = "12")
    Long roomId,

    @Schema(description = "발신자 ID", example = "1")
    Long senderId,

    @Schema(description = "메시지 내용", example = "네, 감사합니다!")
    String message,

    @Schema(description = "전송 시각", example = "2025-09-16T11:05:00")
    LocalDateTime sentAt
) {
}
