package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 좋아요 토글 결과")
public record TrainerLikeToggleResponse(
    @Schema(description = "좋아요 여부", example = "true")
    boolean followed,

    @Schema(description = "처리 메시지", example = "좋아요 상태가 변경되었습니다.")
    String message
) {
}
