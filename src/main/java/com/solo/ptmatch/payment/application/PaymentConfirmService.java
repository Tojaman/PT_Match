package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsClient;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsException;
import com.solo.ptmatch.payment.infrastructure.toss.TossServerException;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmResponse;
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

        TossConfirmResponse confirmResponse;
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

        
        validateAmountOrReject(preConfirmResult, confirmResponse); // 금액 검증

        // 3. 결제 성공 -> 매칭 및 스케줄 확정
        try {
            return paymentConfirmTxService.finalizeSuccess(
                    preConfirmResult.orderId(),
                    confirmResponse.paymentKey(),
                    confirmResponse.approvedAt().toLocalDateTime());
        } catch (RuntimeException exception) {
            log.error("승인 성공 후 로컬 반영 실패. orderId={}", preConfirmResult.orderId(), exception);
            return paymentConfirmTxService.markCancelPending(
                    preConfirmResult.orderId(),
                    confirmResponse.paymentKey(),
                    "LOCAL_FINALIZE_FAILED",
                    "승인 성공 후 로컬 반영에 실패해 취소 보상을 진행합니다.",
                    "LOCAL_FINALIZE_FAILED",
                    "CONFIRM_FINALIZE",
                    LocalDateTime.now());
        }
    }

    // 클라이언트 polling 용도
    public PaymentOrderStatusResponse getOrderStatus(String userEmail, String orderId) {
        return paymentConfirmTxService.getOrderStatus(userEmail, orderId);
    }

    private void validateAmountOrReject(PreConfirmResult preConfirmResult, TossConfirmResponse confirmResponse) {
        if (preConfirmResult.amount().equals(confirmResponse.totalAmount())) {
            return;
        }

        paymentConfirmTxService.finalizeRejected(
                preConfirmResult.orderId(),
                "PAYMENT_AMOUNT_MISMATCH",
                "승인 응답 금액이 주문 금액과 일치하지 않습니다.");
        throw GlobalException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }
}
