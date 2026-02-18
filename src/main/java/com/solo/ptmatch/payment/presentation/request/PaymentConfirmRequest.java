package com.solo.ptmatch.payment.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotBlank
        String orderId,

        @NotBlank
        String paymentKey,

        @NotNull
        @Positive
        Integer amount
) {
}
