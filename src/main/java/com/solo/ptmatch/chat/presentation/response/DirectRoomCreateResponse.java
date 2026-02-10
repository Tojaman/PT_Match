package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "1:1 채팅방 생성/조회 결과")
public record DirectRoomCreateResponse(
        @Schema(description = "채팅방 ID", example = "12") Long roomId,

        @Schema(description = "새로 생성 여부 (true: 신규 생성, false: 기존 방 반환)") boolean created) {
}
