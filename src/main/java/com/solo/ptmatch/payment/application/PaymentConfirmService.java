package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsClient;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsException;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmResponse;
import com.solo.ptmatch.payment.presentation.request.PaymentConfirmRequest;
import com.solo.ptmatch.payment.presentation.response.PaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private final PaymentConfirmTxService paymentConfirmTxService;
    private final TossPaymentsClient tossPaymentsClient;

    public PaymentConfirmResponse confirm(String userEmail, PaymentConfirmRequest request) {
        PreConfirmResult preConfirmResult = paymentConfirmTxService.preConfirm(userEmail, request);
        if (preConfirmResult.isAlreadyDone()) {
            return preConfirmResult.doneResponse();
        }

        TossConfirmResponse confirmResponse;
        try {
            confirmResponse = tossPaymentsClient.confirm(request.paymentKey(), request.orderId(), request.amount());
        } catch (TossPaymentsException exception) {
            paymentConfirmTxService.finalizeRejected(
                    preConfirmResult.orderId(),
                    exception.getProviderCode(),
                    exception.getProviderMessage());

            if (exception.isClientError()) {
                throw GlobalException.of(ErrorCode.INVALID_REQUEST);
            }
            throw GlobalException.of(ErrorCode.PAYMENT_PROVIDER_ERROR);
        }

        if (!preConfirmResult.amount().equals(confirmResponse.totalAmount())) {
            paymentConfirmTxService.finalizeRejected(
                    preConfirmResult.orderId(),
                    "PAYMENT_AMOUNT_MISMATCH",
                    "승인 응답 금액이 주문 금액과 일치하지 않습니다.");
            throw GlobalException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        if (confirmResponse.approvedAt() == null) {
            paymentConfirmTxService.finalizeRejected(
                    preConfirmResult.orderId(),
                    "INVALID_PROVIDER_RESPONSE",
                    "승인 시각이 없습니다.");
            throw GlobalException.of(ErrorCode.PAYMENT_PROVIDER_ERROR);
        }

        return paymentConfirmTxService.finalizeSuccess(
                preConfirmResult.orderId(),
                confirmResponse.paymentKey(),
                confirmResponse.approvedAt().toLocalDateTime());
    }
}
