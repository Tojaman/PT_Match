package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.infrastructure.PaymentOrderRepository;
import com.solo.ptmatch.payment.presentation.request.TossPaymentWebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentConfirmTxService paymentConfirmTxService;

    public void handlePaymentStatusChanged(TossPaymentWebhookRequest request) {
        TossPaymentWebhookRequest.PaymentStatusChangedData data = request.data();
        String orderId = data.orderId();
        String status = data.status();

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(orderId).orElse(null);
        if (paymentOrder == null) {
            return;
        }

        if (paymentOrder.isTerminal()) {
            return;
        }

        switch (status.toUpperCase()) {
            case "DONE" -> paymentConfirmTxService.finalizeSuccess(
                    orderId,
                    paymentOrder.getPaymentKey(),
                    data.approvedAt().toLocalDateTime());
            case "EXPIRED" -> paymentConfirmTxService.finalizeExpired(orderId, "EXPIRED", "웹훅으로 주문 만료가 확인되었습니다.");
            case "CANCELED" -> paymentConfirmTxService.finalizeCanceled(orderId, "CANCELED", "웹훅으로 주문 취소가 확인되었습니다.");
            case "ABORTED" -> paymentConfirmTxService.finalizeRejected(orderId, "ABORTED", "웹훅으로 주문 실패가 확인되었습니다.");
            default -> log.warn("[Webhook] 처리하지 않는 status. orderId={}, status={}", orderId, status);
        }
    }
}
