package com.solo.ptmatch.matching.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매칭 응답 결과")
public record MatchingRespondResponse(
    @Schema(description = "처리 메시지", example = "요청이 처리되었습니다.")
    String message,

    @Schema(description = "생성된 채팅방 ID", example = "12")
    Long chatRoomId
) {
}
