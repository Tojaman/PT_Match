package com.solo.ptmatch.review.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "리뷰 수정 요청")
public record ReviewUpdateRequest(
    @Schema(description = "수정할 별점", example = "5")
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating,

    @Schema(description = "수정할 리뷰 내용", example = "꾸준한 관리를 받으며 목표를 달성했어요!")
    @NotBlank
    @Size(max = 1000)
    String content
) {
}

