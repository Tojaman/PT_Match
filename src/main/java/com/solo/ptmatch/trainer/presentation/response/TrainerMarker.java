package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도 마커 응답")
public record TrainerMarker(
        @Schema(description = "트레이너 ID", example = "1")
        Long trainerId,

        @Schema(description = "위도")
        double latitude,

        @Schema(description = "경도")
        double longitude,

        @Schema(description = "트레이너 이름", example = "박전문")
        String trainerName,

        @Schema(description = "시설명", example = "강남 PT 센터")
        String facilityName,

        @Schema(description = "회당 가격", example = "50000")
        double pricePerSession,

        @Schema(description = "경력 년수", example = "6")
        int careerYears) {

    public static TrainerMarker from(TrainerProfile trainer) {
        return new TrainerMarker(
                trainer.getId(),
                trainer.getLatitude(),
                trainer.getLongitude(),
                trainer.getUser().getName(),
                trainer.getFacilityName(),
                trainer.getPricePerSession() == null ? 0d : trainer.getPricePerSession(),
                trainer.getCareerYears());
    }
}
