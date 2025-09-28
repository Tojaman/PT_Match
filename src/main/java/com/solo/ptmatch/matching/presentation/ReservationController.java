package com.solo.ptmatch.matching.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.matching.application.ReservationService;
import com.solo.ptmatch.matching.presentation.request.ReservationCreateRequest;
import com.solo.ptmatch.matching.presentation.response.ReservationCancelResponse;
import com.solo.ptmatch.matching.presentation.response.ReservationDetailResponse;
import com.solo.ptmatch.matching.presentation.response.ReservationSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성", description = "사용자가 매칭을 기반으로 예약을 생성한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 생성 성공")
    @PostMapping
    public ApiResponse<ReservationDetailResponse> createReservation(
        @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationDetailResponse response = reservationService.createReservation(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 예약 목록", description = "사용자가 자신의 예약 목록을 상태 필터로 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 예약 목록 조회 성공")
    @GetMapping("/me")
    public ApiResponse<List<ReservationSummaryResponse>> getMyReservations(
        @RequestParam(required = false) String status
    ) {
        List<ReservationSummaryResponse> response = reservationService.getMyReservations(status);
        return ApiResponse.success(response);
    }

    @Operation(summary = "예약 상세 조회", description = "단일 예약의 상세 정보를 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 상세 조회 성공")
    @GetMapping("/{reservationId}")
    public ApiResponse<ReservationDetailResponse> getReservationDetail(@PathVariable Long reservationId) {
        ReservationDetailResponse response = reservationService.getReservationDetail(reservationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "예약 취소", description = "사용자가 예약을 취소한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 취소 성공")
    @DeleteMapping("/{reservationId}")
    public ApiResponse<ReservationCancelResponse> cancelReservation(@PathVariable Long reservationId) {
        ReservationCancelResponse response = reservationService.cancelReservation(reservationId);
        return ApiResponse.success(response);
    }
}
