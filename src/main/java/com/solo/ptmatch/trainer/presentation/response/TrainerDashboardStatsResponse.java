package com.solo.ptmatch.trainer.presentation.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record TrainerDashboardStatsResponse(
        int todaySessions,
        int activeMembers,
        int pendingRequests,
        BigDecimal reviewRating) {

    public static TrainerDashboardStatsResponse of(int todaySessions, int activeMembers, int pendingRequests, BigDecimal reviewRating) {
        return new TrainerDashboardStatsResponse(todaySessions, activeMembers, pendingRequests, reviewRating);
    }
}
