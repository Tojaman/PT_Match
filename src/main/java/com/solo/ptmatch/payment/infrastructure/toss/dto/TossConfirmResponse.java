package com.solo.ptmatch.payment.infrastructure.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossConfirmResponse(
                String paymentKey,
                String orderId,
                Integer totalAmount,
                String status,
                OffsetDateTime approvedAt) {
        public LocalDateTime approvedLocalDateTime() {
                return approvedAt == null ? null : approvedAt.toLocalDateTime();
        }
}
