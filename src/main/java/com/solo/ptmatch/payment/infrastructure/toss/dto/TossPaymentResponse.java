package com.solo.ptmatch.payment.infrastructure.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        Integer totalAmount,
        String status,
        OffsetDateTime approvedAt,
        List<CancelDetail> cancels) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CancelDetail(
            Integer cancelAmount,
            String cancelStatus,
            String canceledAt) {
    }

    public LocalDateTime approvedLocalDateTime() {
        return approvedAt == null ? null : approvedAt.toLocalDateTime();
    }
}
