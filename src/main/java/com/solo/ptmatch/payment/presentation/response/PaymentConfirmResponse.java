package com.solo.ptmatch.payment.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.domain.PaymentStatus;
import java.time.LocalDateTime;

public record PaymentConfirmResponse(
        Long matchingId,
        String orderId,
        String paymentKey,
        PaymentStatus paymentStatus,
        MatchingStatus matchingStatus,
        LocalDateTime approvedAt
) {

    public static PaymentConfirmResponse from(PaymentOrder paymentOrder) {
        return new PaymentConfirmResponse(
                paymentOrder.getMatching().getId(),
                paymentOrder.getOrderId(),
                paymentOrder.getPaymentKey(),
                paymentOrder.getStatus(),
                paymentOrder.getMatching().getMatchingStatus(),
                paymentOrder.getApprovedAt());
    }
}
