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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(summary = "PT 매칭 신청", description = "사용자가 PT 매칭을 신청한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매칭 신청 성공")
    @PostMapping("/request")
    public ApiResponse<MatchingRequestCreateResponse> requestMatching(
        @Valid @RequestBody MatchingRequestCreateRequest request
    ) {
        MatchingRequestCreateResponse response = matchingService.requestMatching(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "보낸 매칭 신청 목록", description = "사용자가 보낸 매칭 신청 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "보낸 매칭 신청 목록 조회 성공")
    @GetMapping("/sent")
    public ApiResponse<List<MatchingSentSummaryResponse>> getSentMatchings() {
        List<MatchingSentSummaryResponse> response = matchingService.getSentMatchings();
        return ApiResponse.success(response);
    }

    @Operation(summary = "받은 매칭 신청 목록", description = "트레이너가 받은 매칭 신청 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "받은 매칭 신청 목록 조회 성공")
    @GetMapping("/received")
    public ApiResponse<List<MatchingReceivedSummaryResponse>> getReceivedMatchings() {
        List<MatchingReceivedSummaryResponse> response = matchingService.getReceivedMatchings();
        return ApiResponse.success(response);
    }

    @Operation(summary = "매칭 신청 응답", description = "트레이너가 매칭 신청을 수락 또는 거절한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매칭 응답 처리 성공")
    @PutMapping("/{matchingId}/respond")
    public ApiResponse<MatchingRespondResponse> respondMatching(
        @PathVariable Long matchingId,
        @Valid @RequestBody MatchingRespondRequest request
    ) {
        MatchingRespondResponse response = matchingService.respondMatching(matchingId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{matchingId}")
    @Operation(summary = "매칭 상세 조회", description = "단일 매칭의 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    public ApiResponse<MatchingDetailResponse> getMatching(@PathVariable Long matchingId) {
        return ApiResponse.success(matchingService.getMatching(matchingId));
    }
}
