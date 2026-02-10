package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "채팅방 목록 응답 (커서 기반 페이징)")
public record ChatRoomsResponse(
        @Schema(description = "채팅방 요약 목록") List<ChatRoomSummaryResponse> rooms,

        @Schema(description = "다음 커서 (다음 페이지 요청 시 사용, null이면 마지막 페이지)") Long nextCursorRoomId,

        @Schema(description = "다음 페이지 존재 여부") boolean hasNext) {
}
