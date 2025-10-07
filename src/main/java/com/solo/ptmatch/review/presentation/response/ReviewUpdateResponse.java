package com.solo.ptmatch.review.presentation.response;

import com.solo.ptmatch.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "리뷰 수정 결과")
public record ReviewUpdateResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "수정된 별점", example = "4")
    Integer rating,

    @Schema(description = "수정된 리뷰 내용", example = "꾸준한 관리 덕에 체지방이 많이 줄었어요.")
    String content,

    @Schema(description = "수정 일시", example = "2025-09-15T10:21:30")
    LocalDateTime updatedAt
) {

    public static ReviewUpdateResponse from(Review review) {
        return new ReviewUpdateResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getUpdatedAt()
        );
    }
}

