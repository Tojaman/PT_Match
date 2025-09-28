package com.solo.ptmatch.chat.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 페이지 정보")
public record ChatPageInfoResponse(
    @Schema(description = "페이지 번호", example = "1")
    int page,

    @Schema(description = "페이지 크기", example = "20")
    int size,

    @Schema(description = "전체 개수", example = "101")
    long totalElements,

    @Schema(description = "전체 페이지", example = "6")
    int totalPages
) {
}
