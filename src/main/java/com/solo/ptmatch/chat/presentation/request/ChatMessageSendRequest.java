package com.solo.ptmatch.chat.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "채팅 메시지 전송 요청")
public record ChatMessageSendRequest(
    @Schema(description = "클라이언트 메시지 식별자(멱등키)", example = "2fc6a102-3a79-4d96-9b0f-a25e6b2b1597")
    @Size(max = 64)
    String clientMessageId,

    @Schema(description = "메시지 내용", example = "네, 감사합니다!")
    @NotBlank
    @Size(max = 1000)
    String content
) {
}
