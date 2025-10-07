package com.solo.ptmatch.trainer.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Schema(description = "트레이너 검색 조건")
public record TrainerSearchRequest(
    @Schema(description = "전문 분야 필터", example = "다이어트")
    String specialty,

    @Schema(description = "지역 필터", example = "서울")
    String region,

    @Schema(description = "정렬 기준", example = "rating_desc")
    @Pattern(regexp = "^[a-zA-Z]+_(asc|desc)$", message = "정렬 기준은 '프로퍼티_asc' 또는 '프로퍼티_desc' 형식이어야 합니다.")
    String sort,

    @Schema(description = "페이지 번호", example = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    Integer page,

    @Schema(description = "페이지 크기", example = "10")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 최대 20입니다.")
    Integer size
) {
}
