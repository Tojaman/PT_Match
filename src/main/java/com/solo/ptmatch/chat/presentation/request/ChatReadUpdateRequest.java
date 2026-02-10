package com.solo.ptmatch.chat.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "읽음 상태 갱신 요청")
public record ChatReadUpdateRequest(
        @Schema(description = "마지막으로 읽은 메시지 ID", example = "201") @NotNull Long lastReadMessageId) {
}
