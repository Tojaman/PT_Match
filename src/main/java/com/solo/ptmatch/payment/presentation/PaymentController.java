package com.solo.ptmatch.payment.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.payment.application.PaymentConfirmService;
import com.solo.ptmatch.payment.application.PaymentPrepareService;
import com.solo.ptmatch.payment.domain.PaymentStatus;
import com.solo.ptmatch.payment.presentation.request.PaymentConfirmRequest;
import com.solo.ptmatch.payment.presentation.request.PaymentPrepareRequest;
import com.solo.ptmatch.payment.presentation.response.PaymentConfirmResponse;
import com.solo.ptmatch.payment.presentation.response.PaymentOrderStatusResponse;
import com.solo.ptmatch.payment.presentation.response.PaymentPrepareResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "매칭 결제 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/matchings/payments")
public class PaymentController {

    private final PaymentPrepareService paymentPrepareService;
    private final PaymentConfirmService paymentConfirmService;

    @Operation(summary = "결제 준비", description = "매칭 결제를 시작하기 위한 주문 정보와 결제 금액을 생성한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "결제 준비 성공")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> prepare(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody PaymentPrepareRequest request) {
        PaymentPrepareResponse response = paymentPrepareService.prepare(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "결제 승인", description = "결제 완료 후 주문 ID, 결제 키, 결제 금액으로 결제를 승인한다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 승인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "결제 상태 확인 필요")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirm(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody PaymentConfirmRequest request) {
        PaymentConfirmResponse response = paymentConfirmService.confirm(email, request);
        HttpStatus status = response.paymentStatus() == PaymentStatus.UNKNOWN
                ? HttpStatus.ACCEPTED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(response));
    }

    @Operation(summary = "주문 결제 상태 조회", description = "주문 ID로 결제 상태와 매칭 반영 상태를 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 결제 상태 조회 성공")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PaymentOrderStatusResponse>> getOrderStatus(
            @AuthenticationPrincipal(expression = "username") String email,
            @Parameter(description = "주문 ID", example = "ORDER_20260311_0001")
            @PathVariable String orderId) {
        PaymentOrderStatusResponse response = paymentConfirmService.getOrderStatus(email, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
