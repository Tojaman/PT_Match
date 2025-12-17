package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "후기 정보")
public record ReviewInfo(
        Long reviewId,
        int rating,
        String content) {
    public static ReviewInfo from(Review review) {
        return new ReviewInfo(
                review.getId(),
                review.getRating(),
                review.getContent());
    }
}
