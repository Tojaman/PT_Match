package com.solo.ptmatch.payment.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.domain.PaymentStatus;
import java.time.LocalDateTime;

public record PaymentOrderStatusResponse(
        Long matchingId,
        String orderId,
        String paymentKey,
        PaymentStatus paymentStatus,
        MatchingStatus matchingStatus,
        LocalDateTime approvedAt,
        int attemptCount,
        LocalDateTime nextRetryAt,
        LocalDateTime resolveDeadlineAt
) {

    public static PaymentOrderStatusResponse from(PaymentOrder paymentOrder) {
        return new PaymentOrderStatusResponse(
                paymentOrder.getMatching() == null ? null : paymentOrder.getMatching().getId(),
                paymentOrder.getOrderId(),
                paymentOrder.getPaymentKey(),
                paymentOrder.getStatus(),
                paymentOrder.getMatching() == null ? null : paymentOrder.getMatching().getMatchingStatus(),
                paymentOrder.getApprovedAt(),
                paymentOrder.getAttemptCount(),
                paymentOrder.getNextRetryAt(),
                paymentOrder.getResolveDeadlineAt());
    }
}
