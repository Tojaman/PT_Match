package com.solo.ptmatch.review.presentation.response;

import com.solo.ptmatch.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 리뷰 요약")
public record MyReviewSummaryResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "별점", example = "5")
    Integer rating,

    @Schema(description = "리뷰 내용", example = "정말 친절하고 전문적이세요!")
    String content,

    @Schema(description = "작성 일시", example = "2025-09-10T14:00:00")
    LocalDateTime createdAt,

    @Schema(description = "매칭 ID", example = "10")
    Long matchingId,

    @Schema(description = "트레이너 ID", example = "5")
    Long trainerId,

    @Schema(description = "트레이너 이름", example = "김트레이너")
    String trainerName
) {
    public static MyReviewSummaryResponse from(Review review) {
        return new MyReviewSummaryResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getMatching().getId(),
                review.getMatching().getTrainerProfile().getId(),
                review.getMatching().getTrainerProfile().getUser().getName());
    }
}
