package com.solo.ptmatch.payment.application;

public record CompensationExecutionTarget(
        Long compensationJobId,
        String orderId,
        String paymentKey
) {
}

