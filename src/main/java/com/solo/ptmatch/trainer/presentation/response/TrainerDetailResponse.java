package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.application.dto.TrainerCertificationResult;
import com.solo.ptmatch.trainer.application.dto.TrainerDetailResult;
import com.solo.ptmatch.trainer.application.dto.TrainerReviewSnippetResult;
import com.solo.ptmatch.trainer.application.dto.TrainerScheduleResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Schema(description = "스케줄 목록")
    List<ScheduleResponse> schedules,
    @Schema(description = "리뷰 요약 목록")
    List<ReviewResponse> reviews,
    @Schema(description = "자격증 목록")
    List<CertificationResponse> certifications
) {

    public static TrainerDetailResponse from(TrainerDetailResult result) {
        return new TrainerDetailResponse(
            result.trainerId(),
            result.name(),
            result.bio(),
            joinSpecialties(result.specialties()),
            result.careerYears(),
            result.gymAddress(),
            result.profileImageUrl(),
            result.likesCount(),
            result.averageRating(),
            result.schedules().stream().map(ScheduleResponse::from).toList(),
            result.reviews().stream().map(ReviewResponse::from).toList(),
            result.certifications().stream().map(CertificationResponse::from).toList()
        );
    }

    private static String joinSpecialties(Iterable<String> specialties) {
        StringJoiner joiner = new StringJoiner(", ");
        specialties.forEach(joiner::add);
        return joiner.toString();
    }

    @Schema(description = "트레이너 스케줄 응답")
    public record ScheduleResponse(
        @Schema(description = "요일", example = "MON")
        String day,
        @Schema(description = "시작 시간", example = "09:00")
        String startTime,
        @Schema(description = "종료 시간", example = "18:00")
        String endTime
    ) {

        private static ScheduleResponse from(TrainerScheduleResult result) {
            return new ScheduleResponse(result.day(), result.startTime(), result.endTime());
        }
    }

    @Schema(description = "리뷰 요약 응답")
    public record ReviewResponse(
        @Schema(description = "리뷰 ID", example = "1")
        Long reviewId,
        @Schema(description = "리뷰 작성자", example = "김회원")
        String reviewerName,
        @Schema(description = "별점", example = "5")
        int rating,
        @Schema(description = "리뷰 내용")
        String content
    ) {

        private static ReviewResponse from(TrainerReviewSnippetResult result) {
            return new ReviewResponse(result.reviewId(), result.reviewerName(), result.rating(), result.content());
        }
    }

    @Schema(description = "자격증 응답")
    public record CertificationResponse(
        @Schema(description = "자격증 ID", example = "1")
        Long certificationId,
        @Schema(description = "자격증명")
        String name,
        @Schema(description = "발급 기관")
        String issuingOrganization,
        @Schema(description = "취득일", example = "2023-01-01")
        LocalDate acquisitionDate
    ) {

        private static CertificationResponse from(TrainerCertificationResult result) {
            return new CertificationResponse(result.certificationId(), result.name(), result.issuingOrganization(), result.acquisitionDate());
        }
    }
}
