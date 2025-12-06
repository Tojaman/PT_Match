package com.solo.ptmatch.review.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "트레이너 후기 목록 응답")
public record TrainerReviewSummaryResponse(
        @Schema(description = "리뷰 ID", example = "1") Long id,
        @Schema(description = "작성자 이름", example = "김회원") String reviewerName,
        @Schema(description = "평점", example = "5") int rating,
        @Schema(description = "내용", example = "친절하게 잘 가르쳐주세요") String content,
        @Schema(description = "작성일시") LocalDateTime createdAt) {
}
