package com.solo.ptmatch.trainer.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record TrainerSummaryResult(
    Long trainerId,
    String name,
    List<String> specialties,
    int careerYears,
    BigDecimal averageRating,
    String gymAddress,
    String profileImageUrl,
    Long followerCount,
    Long reviewCount
) {
}
