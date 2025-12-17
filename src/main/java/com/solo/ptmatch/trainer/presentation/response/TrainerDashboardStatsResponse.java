package com.solo.ptmatch.trainer.presentation.response;

import lombok.Builder;

@Builder
public record TrainerDashboardStatsResponse(
        int todaySessions, // 오늘의 수업 수
        int activeMembers, // 진행 중인 회원 수 (ACCEPTED 상태)
        int pendingRequests // 대기 중인 매칭 요청 수 (PENDDING 상태)
) {

    public static TrainerDashboardStatsResponse of(int todaySessions, int activeMembers, int pendingRequests) {
        return new TrainerDashboardStatsResponse(todaySessions, activeMembers, pendingRequests);
    }
}
