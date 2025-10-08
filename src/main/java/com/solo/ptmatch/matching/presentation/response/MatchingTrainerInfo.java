package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 핵심 정보")
public record MatchingTrainerInfo(
    @Schema(description = "트레이너 ID", example = "1")
    Long trainerId,
    @Schema(description = "트레이너 이름", example = "김헬스")
    String trainerName
) {
    public static MatchingTrainerInfo from(TrainerProfile trainerProfile) {
        return new MatchingTrainerInfo(trainerProfile.getUser().getId(), trainerProfile.getUser().getName());
    }
}
