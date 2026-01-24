package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팔로우한 트레이너 요약")
public record LikedTrainerSummaryResponse(
        @Schema(description = "트레이너 ID", example = "1")
        Long trainerId,

        @Schema(description = "트레이너 이름", example = "박전문")
        String name,

        @Schema(description = "시설 주소", example = "서울시 강남구 ...")
        String facilityAddress,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl) {
    public static LikedTrainerSummaryResponse from(TrainerProfile trainerProfile) {
        return new LikedTrainerSummaryResponse(
                trainerProfile.getId(),
                trainerProfile.getUser().getName(),
                trainerProfile.getFacilityAddress(),
                trainerProfile.getProfileImageUrl());
    }
}
