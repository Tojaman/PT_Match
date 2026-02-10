package com.solo.ptmatch.chat.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "1:1 채팅방 생성 요청")
public record DirectRoomCreateRequest(
        @Schema(description = "대상 사용자 ID", example = "15") @NotNull Long targetUserId) {
}
