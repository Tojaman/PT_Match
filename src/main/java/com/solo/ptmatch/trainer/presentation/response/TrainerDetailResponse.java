package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;

@Schema(description = "트레이너 상세 응답")
public record TrainerDetailResponse(
    @Schema(description = "트레이너 ID", example = "1")
    Long trainerId,
    @Schema(description = "트레이너 이름", example = "박전문")
    String name,
    @Schema(description = "자기소개")
    String bio,
    @Schema(description = "전문 분야", example = "다이어트, 근력강화")
    String specialties,
    @Schema(description = "경력 년수", example = "10")
    int careerYears,
    @Schema(description = "활동 지점")
    String gymAddress,
    @Schema(description = "프로필 이미지 URL")
    String profileImageUrl,
    @Schema(description = "좋아요/팔로우 수")
    Long likesCount,
    @Schema(description = "평균 평점")
    BigDecimal averageRating,
    @Schema(description = "자격증 목록")
    List<TrainerCertificationResponse> certifications
) {

    public static TrainerDetailResponse from(
            TrainerProfile profile,
            List<TrainerCertificationResponse> certifications
    ) {
        return new TrainerDetailResponse(
                profile.getId(),
                profile.getTrainer().getName(),
                profile.getBio(),
                profile.getSpecialty().name(),
                profile.getCareerYears(),
                profile.getGymAddress(),
                profile.getProfileImageUrl(),
                (long) profile.getFollowersCount(),
                profile.getAverageRating(),
                certifications
        );
    }

    private static String joinSpecialties(Iterable<String> specialties) {
        StringJoiner joiner = new StringJoiner(", ");
        specialties.forEach(joiner::add);
        return joiner.toString();
    }

}
