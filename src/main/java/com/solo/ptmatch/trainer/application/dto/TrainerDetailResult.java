package com.solo.ptmatch.trainer.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record TrainerDetailResult(
    Long trainerId,
    String name,
    String bio,
    List<String> specialties,
    int careerYears,
    String gymAddress,
    String profileImageUrl,
    Long likesCount,
    BigDecimal averageRating,
    List<TrainerScheduleResult> schedules,
    List<TrainerReviewSnippetResult> reviews,
    List<TrainerCertificationResult> certifications
) {
}
