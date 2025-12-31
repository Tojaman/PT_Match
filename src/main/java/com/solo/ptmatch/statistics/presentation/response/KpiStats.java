package com.solo.ptmatch.statistics.presentation.response;

import java.math.BigDecimal;

public record KpiStats(
                int activeMembers,
                String activeMembersTrend,
                int newMembers,
                int reRegisteredMembers,
                String registrationRate,
                int completedSessions,
                String sessionsGoalRate,
                BigDecimal expectedRevenue,
                String revenueTrend) {
	public static KpiStats of(
					int activeMembers,
					String activeMembersTrend,
					int newMembers,
					int reRegisteredMembers,
					String registrationRate,
					int completedSessions,
					String sessionsGoalRate,
					BigDecimal expectedRevenue,
					String revenueTrend) {
		return new KpiStats(
						activeMembers, activeMembersTrend, newMembers, reRegisteredMembers,
						registrationRate, completedSessions, sessionsGoalRate, expectedRevenue, revenueTrend);
	}

	public static KpiStats empty() {
		return new KpiStats(0, "0", 0, 0, "0%", 0, "0%", BigDecimal.ZERO, "0%");
	}
}
