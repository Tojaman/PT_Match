package com.solo.ptmatch.statistics.presentation.response;

import java.math.BigDecimal;

public record MonthlyPerformanceItem(
                String month,
                BigDecimal revenue,
                int sessions) {
    public static MonthlyPerformanceItem of(String month, BigDecimal revenue, int sessions) {
        return new MonthlyPerformanceItem(month, revenue, sessions);
    }
}
