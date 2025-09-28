package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 요약")
public record ChatMessageResponse(
    @Schema(description = "메시지 ID", example = "101")
    Long messageId,

    @Schema(description = "발신자 ID", example = "15")
    Long senderId,

    @Schema(description = "메시지 내용", example = "네, 안녕하세요!")
    String message,

    @Schema(description = "전송 시각", example = "2025-09-16T11:00:00")
    LocalDateTime sentAt
) {
}
