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
}
