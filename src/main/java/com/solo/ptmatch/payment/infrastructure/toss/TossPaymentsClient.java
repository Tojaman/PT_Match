package com.solo.ptmatch.payment.infrastructure.toss;

import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmResponse;

public interface TossPaymentsClient {

    TossConfirmResponse confirm(String paymentKey, String orderId, int amount);

    TossConfirmResponse getPaymentByOrderId(String orderId);

    TossConfirmResponse cancel(String paymentKey, String cancelReason);
}
