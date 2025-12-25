package com.solo.ptmatch.trainer.presentation.response;

public record MonthlyPerformanceDto(int month, int sessionCount) {
    public static MonthlyPerformanceDto of(int month, int sessionCount) {
        return new MonthlyPerformanceDto(month, sessionCount);
    }
}
