package com.solo.ptmatch.trainer.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 검색 조건")
public record TrainerSearchRequest(
    @Schema(description = "전문 분야 필터", example = "다이어트")
    String specialty,

    @Schema(description = "지역 필터", example = "서울")
    String region,

    @Schema(description = "정렬 기준", example = "rating_desc")
    String sort,

    @Schema(description = "페이지 번호", example = "0")
    Integer page,

    @Schema(description = "페이지 크기", example = "10")
    Integer size
) {
}
