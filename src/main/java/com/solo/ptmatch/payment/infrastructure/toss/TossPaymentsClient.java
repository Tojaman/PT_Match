package com.solo.ptmatch.payment.infrastructure.toss;

import com.solo.ptmatch.payment.infrastructure.toss.dto.TossPaymentResponse;

public interface TossPaymentsClient {

    TossPaymentResponse confirm(String paymentKey, String orderId, int amount);

    TossPaymentResponse getPaymentByOrderId(String orderId);

    TossPaymentResponse cancel(String paymentKey, String cancelReason);
}
