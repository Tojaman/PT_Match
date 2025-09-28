package com.solo.ptmatch.chat.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "채팅 메시지 전송 요청")
public record ChatMessageSendRequest(
    @Schema(description = "메시지 내용", example = "네, 감사합니다!")
    @NotBlank
    @Size(max = 1000)
    String message
) {
}
