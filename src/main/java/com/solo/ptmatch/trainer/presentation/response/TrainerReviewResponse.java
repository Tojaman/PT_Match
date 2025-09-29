package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 요약 응답")
public record TrainerReviewResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,
    @Schema(description = "리뷰 작성자", example = "김회원")
    String reviewerName,
    @Schema(description = "별점", example = "5")
    int rating,
    @Schema(description = "리뷰 내용")
    String content
) {
}