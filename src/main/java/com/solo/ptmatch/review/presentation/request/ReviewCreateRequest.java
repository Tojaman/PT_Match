package com.solo.ptmatch.review.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "리뷰 작성 요청")
public record ReviewCreateRequest(
    @Schema(description = "매칭 ID", example = "1")
    @NotNull
    Long matchingId,

    @Schema(description = "별점", example = "5")
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating,

    @Schema(description = "리뷰 내용", example = "정말 친절하고 전문적이세요!")
    @NotBlank
    @Size(max = 1000)
    String content
) {
}
