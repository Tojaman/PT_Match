package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsClient;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsException;
import com.solo.ptmatch.payment.infrastructure.toss.TossServerException;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossPaymentResponse;
import com.solo.ptmatch.payment.presentation.request.PaymentConfirmRequest;
import com.solo.ptmatch.payment.presentation.response.PaymentConfirmResponse;
import com.solo.ptmatch.payment.presentation.response.PaymentOrderStatusResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentConfirmService {

    private final PaymentConfirmTxService paymentConfirmTxService;
    private final TossPaymentsClient tossPaymentsClient;

    public PaymentConfirmResponse confirm(String userEmail, PaymentConfirmRequest request) {
        // 1. 결제 정보 조회 및 상태 검증
        PreConfirmResult preConfirmResult = paymentConfirmTxService.preConfirm(userEmail, request);
        if (preConfirmResult.isAlreadyDone()) {
            return preConfirmResult.doneResponse();
        }

        TossPaymentResponse confirmResponse;
        try {
            // 2. 토스 페이먼츠 결제 승인 API 호출
            confirmResponse = tossPaymentsClient.confirm(request.paymentKey(), request.orderId(), request.amount());
        } catch (TossServerException exception) { // 5xx (타임아웃, 네트워크 오류 등) -> 재시도
            return paymentConfirmTxService.markUnknown(
                    preConfirmResult.orderId(),
                    exception.getProviderCode(),
                    exception.getProviderMessage(),
                    LocalDateTime.now());
        } catch (TossPaymentsException exception) { // 4xx (실패)
            paymentConfirmTxService.finalizeRejected(
                    preConfirmResult.orderId(),
                    exception.getProviderCode(),
                    exception.getProviderMessage());
            throw GlobalException.of(ErrorCode.INVALID_REQUEST);
        }

        // 3. 결제 성공 -> 매칭 및 스케줄 확정
        // 실패 시 재시도 (APPROVING)
        return paymentConfirmTxService.finalizeSuccess(
                preConfirmResult.orderId(),
                confirmResponse.paymentKey(),
                confirmResponse.approvedAt().toLocalDateTime());
    }

    // 클라이언트 polling 용도
    public PaymentOrderStatusResponse getOrderStatus(String userEmail, String orderId) {
        return paymentConfirmTxService.getOrderStatus(userEmail, orderId);
    }
}