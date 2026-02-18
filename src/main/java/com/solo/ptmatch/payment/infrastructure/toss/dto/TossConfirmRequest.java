package com.solo.ptmatch.payment.infrastructure.toss.dto;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount
) {
}
