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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/matchings/payments")
public class PaymentController {

    private final PaymentPrepareService paymentPrepareService;
    private final PaymentConfirmService paymentConfirmService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> prepare(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody PaymentPrepareRequest request) {
        PaymentPrepareResponse response = paymentPrepareService.prepare(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirm(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody PaymentConfirmRequest request) {
        PaymentConfirmResponse response = paymentConfirmService.confirm(email, request);
        HttpStatus status = (response.paymentStatus() == PaymentStatus.UNKNOWN
                || response.paymentStatus() == PaymentStatus.CANCEL_PENDING)
                ? HttpStatus.ACCEPTED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PaymentOrderStatusResponse>> getOrderStatus(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable String orderId) {
        PaymentOrderStatusResponse response = paymentConfirmService.getOrderStatus(email, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
