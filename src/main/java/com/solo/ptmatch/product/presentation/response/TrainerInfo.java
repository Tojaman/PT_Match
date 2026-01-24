package com.solo.ptmatch.product.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "연결된 트레이너 정보")
public record TrainerInfo(
        @Schema(description = "트레이너 ID", example = "15")
        Long trainerId,
        
        @Schema(description = "트레이너 이름")
        String trainerName,

        @Schema(description = "시설 주소")
        String facilityAddress) {

    public static TrainerInfo from(TrainerProfile trainerProfile) {
        return new TrainerInfo(
                trainerProfile.getId(),
                trainerProfile.getUser().getName(),
                trainerProfile.getFacilityAddress());
    }
}
