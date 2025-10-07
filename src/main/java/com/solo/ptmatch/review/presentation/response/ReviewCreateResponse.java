package com.solo.ptmatch.review.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

// 쿠팡 리뷰 작성 결과로 reviewId만 내려줌. 따라서 다른 필드 모두 제거
@Schema(description = "리뷰 작성 결과")
public record ReviewCreateResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId
) {

    public static ReviewCreateResponse of(Long reviewId) {
        return new ReviewCreateResponse(reviewId);
    }
}
