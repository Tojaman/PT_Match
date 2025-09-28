package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "채팅 메시지 목록 응답")
public record ChatMessagesResponse(
    @Schema(description = "메시지 목록")
    List<ChatMessageResponse> messages,

    @Schema(description = "페이지 정보")
    ChatPageInfoResponse pageInfo
) {
}
