package com.solo.ptmatch.product.presentation.response;

import com.solo.ptmatch.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 요약 정보")
public record ReviewInfo(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,
    @Schema(description = "리뷰 작성자 이름")
    String reviewerName,
    @Schema(description = "평점", example = "5")
    int rating,
    @Schema(description = "리뷰 내용")
    String content
) {

    public static ReviewInfo from(Review review) {
        return new ReviewInfo(
            review.getId(),
            review.getMatching().getUser().getName(),
            review.getRating(),
            review.getContent()
        );
    }
}
