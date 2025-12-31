package com.solo.ptmatch.statistics.presentation.response;

import java.math.BigDecimal;
import java.util.Map;

public record ReviewSummary(
                BigDecimal averageRating,
                int totalReviews,
                Map<Integer, Long> distribution) {
    public static ReviewSummary of(BigDecimal averageRating, int totalReviews, Map<Integer, Long> distribution) {
        return new ReviewSummary(averageRating, totalReviews, distribution);
    }

    public static ReviewSummary empty() {
        return new ReviewSummary(BigDecimal.ZERO, 0, Map.of());
    }
}
