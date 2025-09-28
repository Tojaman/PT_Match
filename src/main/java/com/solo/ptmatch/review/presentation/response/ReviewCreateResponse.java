package com.solo.ptmatch.review.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 작성 결과")
public record ReviewCreateResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,

    @Schema(description = "처리 메시지", example = "후기가 성공적으로 등록되었습니다.")
    String message
) {
}
