package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팔로우한 트레이너 요약")
public record FollowedTrainerSummaryResponse(
    @Schema(description = "트레이너 ID", example = "1")
    Long trainerId,

    @Schema(description = "트레이너 이름", example = "박전문")
    String name,

    @Schema(description = "헬스장 이름", example = "피트니스 센터")
    String gymName,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl
) {
}
