package com.solo.ptmatch.payment.presentation.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentWebhookRequest(
        String eventType,
        PaymentStatusChangedData data) {
                
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PaymentStatusChangedData(
                String orderId,
                String paymentKey,
                String status,
                OffsetDateTime approvedAt) {
        }
}
