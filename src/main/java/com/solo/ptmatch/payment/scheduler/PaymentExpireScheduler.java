package com.solo.ptmatch.payment.scheduler;

import java.time.LocalDateTime;

import com.solo.ptmatch.payment.application.PaymentConfirmTxService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentExpireScheduler {

    private final PaymentConfirmTxService paymentConfirmTxService;

    // TTL 만료 주문 처리
    @Scheduled(fixedDelayString = "${payment.retry.reconcile-interval}")
    public void expireReadyOrders() {
        LocalDateTime now = LocalDateTime.now();
        paymentConfirmTxService.expireReadyOrder(now);
    }
}

