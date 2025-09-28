package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 팔로우 토글 결과")
public record TrainerFollowToggleResponse(
    @Schema(description = "팔로우 여부", example = "true")
    boolean followed,

    @Schema(description = "처리 메시지", example = "팔로우 상태가 변경되었습니다.")
    String message
) {
}
