package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "트레이너 프로필 등록/수정 응답")
public record TrainerProfileUpsertResponse(
    @Schema(description = "프로필 ID", example = "1")
    Long profileId,
    @Schema(description = "자기소개")
    String bio,
    @Schema(description = "경력 년수")
    int careerYears,
    @Schema(description = "전문 분야 목록")
    List<String> specialties,
    @Schema(description = "활동 지점")
    String gymAddress,
    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl
) {
}
