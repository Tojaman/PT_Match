package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.payment.presentation.response.PaymentConfirmResponse;

record PreConfirmResult(
        String orderId,
        Integer amount,
        PaymentConfirmResponse doneResponse
) {
    static PreConfirmResult alreadyDone(PaymentConfirmResponse doneResponse) {
        return new PreConfirmResult(null, null, doneResponse);
    }

    static PreConfirmResult proceed(String orderId, Integer amount) {
        return new PreConfirmResult(orderId, amount, null);
    }

    boolean isAlreadyDone() {
        return doneResponse != null;
    }
}
