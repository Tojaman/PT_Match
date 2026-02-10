package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "채팅방 요약")
public record ChatRoomSummaryResponse(
    @Schema(description = "채팅방 ID", example = "12")
    Long roomId,

    @Schema(description = "상대방 사용자 ID", example = "15")
    Long partnerUserId,

    @Schema(description = "상대방 이름", example = "박전문")
    String partnerName,

    @Schema(description = "마지막 메시지", example = "네, 안녕하세요!")
    String lastMessage,

    @Schema(description = "마지막 메시지 시각", example = "2025-09-16T11:00:00")
    LocalDateTime lastMessageAt,

    @Schema(description = "읽지 않은 메시지 수", example = "1")
    Long unreadCount
) {
}
