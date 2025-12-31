package com.solo.ptmatch.statistics.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.statistics.application.TrainerMonthlyStatsService;
import com.solo.ptmatch.statistics.presentation.response.MonthlyDashboardResponse;
import com.solo.ptmatch.trainer.application.TrainerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainers/statistics")
public class StatisticsController {

    private final TrainerMonthlyStatsService monthlyStatsService;
    private final TrainerProfileService trainerProfileService;

    @Operation(summary = "월별 통계 대시보드 조회", description = "트레이너의 월별 통계 대시보드 데이터를 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대시보드 조회 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyDashboardResponse>> getMonthlyDashboard(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail) {
        Long trainerProfileId = trainerProfileService.getTrainerId(loggedInEmail);
        MonthlyDashboardResponse response = monthlyStatsService.getDashboardData(trainerProfileId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "월별 통계 수동 집계", description = "현재 월의 통계를 수동으로 집계한다 (테스트용)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "집계 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/monthly/aggregate")
    public ResponseEntity<ApiResponse<String>> aggregateMonthlyStats(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail) {
        LocalDate now = LocalDate.now();
        monthlyStatsService.aggregateAllTrainersMonthlyStats(now.getYear(), now.getMonthValue());
        return ResponseEntity.ok(ApiResponse.success("Stats aggregated successfully"));
    }
}
