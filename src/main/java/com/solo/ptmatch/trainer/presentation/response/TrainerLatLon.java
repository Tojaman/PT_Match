package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 요약 응답")
public record TrainerLatLon(
        @Schema(description = "트레이너 ID", example = "1")
        Long trainerId,

        @Schema(description = "위도")
        double latitude,

        @Schema(description = "경도")
        double longitude) {

    public static TrainerLatLon from(TrainerProfile trainer) {
        return new TrainerLatLon(
                trainer.getId(),
                trainer.getLatitude(),
                trainer.getLongitude());
    }
}
