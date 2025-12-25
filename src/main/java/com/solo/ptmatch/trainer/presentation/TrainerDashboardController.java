package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.TrainerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.solo.ptmatch.trainer.presentation.response.TrainerDashboardStatsResponse;
import com.solo.ptmatch.trainer.presentation.response.MonthlyScheduleResponse;
import com.solo.ptmatch.trainer.presentation.response.DashboardAnalyticsResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainer/dashboard")
public class TrainerDashboardController {

    private final TrainerDashboardService trainerDashboardService;

    @Operation(summary = "대시보드 요약 정보 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대시보드 요약 정보 조회 성공")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TrainerDashboardStatsResponse>> getDashboardSummary(
            @AuthenticationPrincipal(expression = "username") String email) {
        TrainerDashboardStatsResponse response = trainerDashboardService.getDashboardSummary(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "월간 일정 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "월간 일정 조회 성공")
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<MonthlyScheduleResponse>>> getMonthlyScheduleStatus(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam int year,
            @RequestParam int month) {
        List<MonthlyScheduleResponse> response = trainerDashboardService.getMonthlyScheduleStatus(email, year, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "대시보드 통계 분석 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대시보드 통계 분석 조회 성공")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<DashboardAnalyticsResponse>> getDashboardAnalytics(
            @AuthenticationPrincipal(expression = "username") String email) {
        DashboardAnalyticsResponse response = trainerDashboardService.getDashboardAnalytics(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
