package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "트레이너 요약 응답")
public record TrainerSummaryResponse(
    @Schema(description = "트레이너 ID", example = "1")
    Long trainerId,
    @Schema(description = "트레이너 이름", example = "박전문")
    String name,
    @Schema(description = "전문 분야 목록", example = "[\"DIET\", \"STRENGTH_CONDITIONING\"]")
    List<Specialty> specialties,
    @Schema(description = "경력 년수", example = "5")
    int careerYears,
    @Schema(description = "평균 평점", example = "4.8")
    BigDecimal averageRating,
    @Schema(description = "헬스장 이름")
    String gymName,
    @Schema(description = "활동 지점", example = "피트니스 센터")
    String gymAddress,
    @Schema(description = "지점 위도")
    double gymLatitude,
    @Schema(description = "지점 경도")
    double gymLongitude,
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl,
    @Schema(description = "좋아요 수", example = "50")
    int likeCount,
    @Schema(description = "리뷰 수", example = "120")
    int reviewCount,
    @Schema(description = "회당 가격", example = "50000")
    Integer pricePerSession) {

    public static TrainerSummaryResponse from(TrainerProfile trainer) {
        return new TrainerSummaryResponse(
            trainer.getId(),
            trainer.getUser().getName(),
            trainer.getSpecialties().stream()
                    .sorted()
                    .toList(),
            trainer.getCareerYears(),
            trainer.getAverageRating(),
            trainer.getGymName(),
            trainer.getGymAddress(),
            trainer.getGymLatitude(),
            trainer.getGymLongitude(),
            trainer.getProfileImageUrl(),
            trainer.getLikesCount(),
            trainer.getReviewCount(),
            trainer.getPricePerSession());
    }
}
