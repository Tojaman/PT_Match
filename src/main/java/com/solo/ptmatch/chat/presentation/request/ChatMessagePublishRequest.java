package com.solo.ptmatch.chat.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "채팅 메시지 발행 요청")
public record ChatMessagePublishRequest(
    @Schema(description = "채팅방 ID", example = "12")
    @NotNull
    Long roomId,

    @Schema(description = "발신자 ID", example = "1")
    @NotNull
    Long senderId,

    @Schema(description = "메시지 내용", example = "네, 감사합니다!")
    @NotBlank
    @Size(max = 1000)
    String message
) {
}
