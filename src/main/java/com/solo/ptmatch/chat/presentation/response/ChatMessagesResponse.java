package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "채팅 메시지 목록 응답")
public record ChatMessagesResponse(
    @Schema(description = "메시지 목록")
    List<ChatMessageResponse> messages,

    @Schema(description = "다음 조회 커서 메시지 ID", example = "101")
    Long nextCursorMessageId,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext
) {
}
