package com.solo.ptmatch.statistics.presentation.response;

import java.util.List;

public record MonthlyDashboardResponse(
                KpiStats kpiStats,
                List<MonthlyPerformanceItem> monthlyPerformance,
                MemberDistribution memberDistribution,
                ReviewSummary reviewSummary,
                List<RecentReviewItem> recentReviews) {
    public static MonthlyDashboardResponse of(
                    KpiStats kpiStats,
                    List<MonthlyPerformanceItem> monthlyPerformance,
                    MemberDistribution memberDistribution,
                    ReviewSummary reviewSummary,
                    List<RecentReviewItem> recentReviews) {
        return new MonthlyDashboardResponse(kpiStats, monthlyPerformance, memberDistribution, reviewSummary, recentReviews);
    }
}
