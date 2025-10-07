package com.solo.ptmatch.review.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "상품 후기 요약")
public record ProductReviewSummaryResponse(
    @Schema(description = "리뷰 ID", example = "101")
    Long reviewId,

    @Schema(description = "리뷰어 이름", example = "김회원")
    String reviewerName,

    @Schema(description = "별점", example = "5")
    Integer rating,

    @Schema(description = "리뷰 내용", example = "10회 패키지로 체지방 많이 빠졌어요!")
    String content,

    @Schema(description = "작성 일시", example = "2025-09-12T14:23:11")
    LocalDateTime createdAt
) {
}

