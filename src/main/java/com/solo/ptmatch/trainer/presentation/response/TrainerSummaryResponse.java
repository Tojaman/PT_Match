package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "트레이너 요약 응답")
public record TrainerSummaryResponse(
        @Schema(description = "트레이너 ID", example = "1")
        Long trainerId,

        @Schema(description = "트레이너 이름", example = "박전문")
        String name,

        @Schema(description = "스포츠 종목", example = "FITNESS")
        SportType sportType,

        @Schema(description = "경력 년수", example = "5")
        int careerYears,

        @Schema(description = "평균 평점", example = "4.8")
        BigDecimal averageRating,

        @Schema(description = "시설 이름", example = "강남 피트니스")
        String facilityName,

        @Schema(description = "시설 주소", example = "서울시 강남구 ...")
        String facilityAddress,

        @Schema(description = "위도")
        double latitude,

        @Schema(description = "경도")
        double longitude,

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
                trainer.getSportType(),
                trainer.getCareerYears(),
                trainer.getAverageRating(),
                trainer.getFacilityName(),
                trainer.getFacilityAddress(),
                trainer.getLatitude(),
                trainer.getLongitude(),
                trainer.getProfileImageUrl(),
                trainer.getLikesCount(),
                trainer.getReviewCount(),
                trainer.getPricePerSession());
    }
}
