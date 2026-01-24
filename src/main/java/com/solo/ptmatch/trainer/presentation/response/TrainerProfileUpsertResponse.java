package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.Certification;
import com.solo.ptmatch.trainer.domain.GymImage;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerImage;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "트레이너 프로필 등록/수정 응답")
public record TrainerProfileUpsertResponse(
        @Schema(description = "프로필 ID", example = "1") Long profileId,
        @Schema(description = "자기소개") String bio,
        @Schema(description = "경력 년수") int careerYears,
        @Schema(description = "스포츠 종목") SportType sportType,
        @Schema(description = "시설명") String facilityName,
        @Schema(description = "시설 주소") String facilityAddress,
        @Schema(description = "프로필 이미지 URL") String profileImageUrl,
        @Schema(description = "회당 가격") Integer pricePerSession,
        @Schema(description = "트레이너 이미지 목록") List<TrainerImageResponse> trainerImages,
        @Schema(description = "지점 이미지 목록") List<GymImageResponse> gymImages,
        @Schema(description = "자격증 목록") List<TrainerCertificationResponse> certifications) {

    public static TrainerProfileUpsertResponse from(
            TrainerProfile savedProfile,
            List<TrainerImage> trainerImages,
            List<GymImage> gymImages,
            List<Certification> certifications) {
        return new TrainerProfileUpsertResponse(
                savedProfile.getId(),
                savedProfile.getBio(),
                savedProfile.getCareerYears(),
                savedProfile.getSportType(),
                savedProfile.getFacilityName(),
                savedProfile.getFacilityAddress(),
                savedProfile.getProfileImageUrl(),
                savedProfile.getPricePerSession(),
                trainerImages.stream().map(TrainerImageResponse::from).toList(),
                gymImages.stream().map(GymImageResponse::from).toList(),
                certifications.stream().map(TrainerCertificationResponse::from).toList());
    }
}
