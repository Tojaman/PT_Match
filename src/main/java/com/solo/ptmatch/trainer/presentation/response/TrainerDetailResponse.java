package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.Certification;
import com.solo.ptmatch.trainer.domain.GymImage;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerImage;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "트레이너 상세 응답")
public record TrainerDetailResponse(
        @Schema(description = "트레이너 ID", example = "1") Long trainerId,
        @Schema(description = "트레이너 이름", example = "박전문") String name,
        @Schema(description = "자기소개") String bio,
        @Schema(description = "전문 분야 목록", example = "[\"DIET\", \"STRENGTH_CONDITIONING\"]") List<Specialty> specialties,
        @Schema(description = "경력 년수", example = "10") int careerYears,
        @Schema(description = "지점명") String gymName,
        @Schema(description = "활동 지점") String gymAddress,
        @Schema(description = "지점 위도") double gymLatitude,
        @Schema(description = "지점 경도") double gymLongitude,
        @Schema(description = "프로필 이미지 URL") String profileImageUrl,
        @Schema(description = "좋아요/팔로우 수") int likesCount,
        @Schema(description = "리뷰 수", example = "120") int reviewCount,
        @Schema(description = "평균 평점") BigDecimal averageRating,
        @Schema(description = "회당 가격") Integer pricePerSession,
        @Schema(description = "트레이너 이미지 목록") List<TrainerImageResponse> trainerImages,
        @Schema(description = "지점 이미지 목록") List<GymImageResponse> gymImages,
        @Schema(description = "자격증 목록") List<TrainerCertificationResponse> certifications

) {

    public static TrainerDetailResponse from(
            TrainerProfile profile,
            List<TrainerImage> trainerImages,
            List<GymImage> gymImages,
            List<Certification> certifications) {
        return new TrainerDetailResponse(
                profile.getId(),
                profile.getUser().getName() != null ? profile.getUser().getName() : "이름 없음",
                profile.getBio(),
                profile.getSpecialties().stream()
                        .sorted()
                        .toList(),
                profile.getCareerYears(),
                profile.getGymName(),
                profile.getGymAddress(),
                profile.getGymLatitude(),
                profile.getGymLongitude(),
                profile.getProfileImageUrl(),
                profile.getLikesCount(),
                profile.getReviewCount(),
                profile.getAverageRating(),
                profile.getPricePerSession(),
                trainerImages.stream().map(TrainerImageResponse::from).toList(),
                gymImages.stream().map(GymImageResponse::from).toList(),
                certifications.stream().map(TrainerCertificationResponse::from).toList());
    }
}
