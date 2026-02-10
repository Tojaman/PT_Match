package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "트레이너 요약 커서 페이지 응답")
public record TrainerSummaryCursorResponse(
        @Schema(description = "현재 페이지 아이템")
        List<TrainerSummaryResponse> items,

        @Schema(description = "다음 조회 커서(없으면 null)", example = "20")
        Integer nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext) {
}
