package com.solo.ptmatch.matching.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.matching.application.MatchingService;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingReceivedSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRespondResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(summary = "PT 매칭 신청", description = "사용자가 PT 매칭을 신청한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매칭 신청 성공")
    @PostMapping("/request")
    public ApiResponse<MatchingRequestCreateResponse> requestMatching(
        @AuthenticationPrincipal(expression = "username") String email,
        @Valid @RequestBody MatchingRequestCreateRequest request
    ) {
        MatchingRequestCreateResponse response = matchingService.requestMatching(email, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "매칭 신청 응답", description = "트레이너가 매칭 신청을 수락 또는 거절한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "매칭 응답 처리 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/respond/{matchingId}")
    public ApiResponse<Void> respondMatching(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long matchingId,
            @Valid @RequestBody MatchingRespondRequest request
    ) {
        matchingService.respondMatching(matchingId, email, request);
        return ApiResponse.success();
    }

    @Operation(summary = "보낸 매칭 신청 목록", description = "사용자가 보낸 매칭 신청 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "보낸 매칭 신청 목록 조회 성공")
    @GetMapping("/sent")
    public ApiResponse<List<MatchingSentSummaryResponse>> getSentMatchings(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        List<MatchingSentSummaryResponse> response = matchingService.getSentMatchings(email);
        return ApiResponse.success(response);
    }

    @Operation(summary = "받은 매칭 신청 목록", description = "트레이너가 받은 매칭 신청 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "받은 매칭 신청 목록 조회 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/received")
    public ApiResponse<List<MatchingReceivedSummaryResponse>> getReceivedMatchings(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        List<MatchingReceivedSummaryResponse> response = matchingService.getReceivedMatchings(email);
        return ApiResponse.success(response);
    }

    @Operation(summary = "매칭 상세 조회", description = "매칭을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매칭 조회 성공")
    @GetMapping("/{matchingId}")
    public ApiResponse<MatchingDetailResponse> getMatchingDetail(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable("matchingId") Long matchingId
    ) {
        MatchingDetailResponse response = matchingService.getMatchingDetail(email, matchingId);
        return ApiResponse.success(response);
    }
}
