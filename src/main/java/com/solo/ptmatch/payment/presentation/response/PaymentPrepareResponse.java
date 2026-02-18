package com.solo.ptmatch.payment.presentation.response;

public record PaymentPrepareResponse(
        String orderId,
        Integer amount
) {

    public static PaymentPrepareResponse of(
            String orderId,
            Integer amount) {
        return new PaymentPrepareResponse(orderId, amount);
    }
}
