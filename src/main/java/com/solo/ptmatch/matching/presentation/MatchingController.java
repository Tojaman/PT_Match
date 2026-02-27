package com.solo.ptmatch.matching.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.common.response.PageResponse;
import com.solo.ptmatch.matching.application.MatchingService;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingReceivedSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRespondResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingScheduleDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/matchings")
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(summary = "매칭 신청 응답", description = "트레이너가 매칭 신청을 수락 또는 거절한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "매칭 응답 처리 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/{matchingId}")
    public ResponseEntity<Void> respondMatching(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long matchingId,
            @Valid @RequestBody MatchingRespondRequest request
    ) {
        matchingService.respondMatching(matchingId, email, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "보낸 매칭 신청 목록", description = "사용자가 보낸 매칭 신청 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "보낸 매칭 신청 목록 조회 성공")
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<MatchingSentSummaryResponse>>> getSentMatchings(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam(required = false) List<MatchingStatus> status,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MatchingSentSummaryResponse> response = matchingService.getSentMatchings(email, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response.getContent(), PageResponse.from(response)));
    }

    @Operation(summary = "받은 매칭 신청 목록", description = "트레이너가 받은 매칭 신청 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "받은 매칭 신청 목록 조회 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<MatchingReceivedSummaryResponse>>> getReceivedMatchings(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam(required = false) List<MatchingStatus> status,
            @ParameterObject @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<MatchingReceivedSummaryResponse> response = matchingService.getReceivedMatchings(email, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response.getContent(), PageResponse.from(response)));
    }

    @Operation(summary = "매칭 상세 조회", description = "매칭을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매칭 조회 성공")
    @GetMapping("/{matchingId}")
    public ResponseEntity<ApiResponse<MatchingDetailResponse>> getMatchingDetail(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable("matchingId") Long matchingId) {
        MatchingDetailResponse response = matchingService.getMatchingDetail(email, matchingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 전체 스케줄 조회", description = "사용자의 전체 스케줄을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 조회 성공")
    @GetMapping("/schedules/me")
    public ResponseEntity<ApiResponse<List<MatchingScheduleDetailResponse>>> getMyAllSchedules(
            @AuthenticationPrincipal(expression = "username") String email) {
        List<MatchingScheduleDetailResponse> response = matchingService.getMyAllSchedules(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "트레이너 전체 스케줄 조회", description = "트레이너의 전체 매칭 스케줄을 조회한다(대시보드용)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 조회 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/schedules/trainer")
    public ResponseEntity<ApiResponse<List<MatchingScheduleDetailResponse>>> getTrainerSchedules(
            @AuthenticationPrincipal(expression = "username") String email) {
        List<MatchingScheduleDetailResponse> response = matchingService.getTrainerDashboardSchedules(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "매칭 스케줄 완료", description = "트레이너가 매칭 스케줄을 완료 처리한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "스케줄 완료 처리 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/schedules/{scheduleId}/complete")
    public ResponseEntity<Void> completeSchedule(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long scheduleId) {
        matchingService.completeSchedule(scheduleId, email);
        return ResponseEntity.noContent().build();
    }
}
