package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.domain.Specialty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 검색 조건")
public record TrainerSearchRequest(
    @Schema(description = "전문 분야 필터", example = "BODYBUILDING")
    Specialty specialty,

    @Schema(description = "검색어 (트레이너 이름, 소개글 등)", example = "홍길동")
    String keyword
) {
}
