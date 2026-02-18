package com.solo.ptmatch.payment.infrastructure.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        Integer totalAmount,
        String status,
        OffsetDateTime approvedAt
) {
}
