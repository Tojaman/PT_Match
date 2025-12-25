package com.solo.ptmatch.trainer.presentation.response;

import java.util.List;

public record DashboardAnalyticsResponse(
        List<MonthlyPerformanceDto> monthlyPerformance,
        List<LowSessionAlertDto> lowSessionAlerts,
        MemberStatsDto memberStats) {
            
    public static DashboardAnalyticsResponse of(
            List<MonthlyPerformanceDto> monthlyPerformance,
            List<LowSessionAlertDto> lowSessionAlerts,
            MemberStatsDto memberStats) {
        return new DashboardAnalyticsResponse(monthlyPerformance, lowSessionAlerts, memberStats);
    }
}
