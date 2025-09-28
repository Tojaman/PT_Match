package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.application.dto.TrainerSummaryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.StringJoiner;

@Schema(description = "트레이너 요약 응답")
public record TrainerSummaryResponse(
    @Schema(description = "트레이너 ID", example = "1")
    Long trainerId,
    @Schema(description = "트레이너 이름", example = "박전문")
    String name,
    @Schema(description = "전문 분야 문자열", example = "다이어트, 근력강화")
    String specialties,
    @Schema(description = "경력 년수", example = "5")
    int careerYears,
    @Schema(description = "평균 평점", example = "4.8")
    BigDecimal averageRating,
    @Schema(description = "활동 지점", example = "피트니스 센터")
    String gymAddress,
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl,
    @Schema(description = "팔로워 수", example = "50")
    Long followerCount,
    @Schema(description = "리뷰 수", example = "120")
    Long reviewCount
) {

    public static TrainerSummaryResponse from(TrainerSummaryResult result) {
        return new TrainerSummaryResponse(
            result.trainerId(),
            result.name(),
            joinSpecialties(result.specialties()),
            result.careerYears(),
            result.averageRating(),
            result.gymAddress(),
            result.profileImageUrl(),
            result.followerCount(),
            result.reviewCount()
        );
    }

    private static String joinSpecialties(Iterable<String> specialties) {
        StringJoiner joiner = new StringJoiner(", ");
        specialties.forEach(joiner::add);
        return joiner.toString();
    }
}
