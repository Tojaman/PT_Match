package com.solo.ptmatch.payment.presentation;

import com.solo.ptmatch.payment.application.PaymentWebhookService;
import com.solo.ptmatch.payment.presentation.request.TossPaymentWebhookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/webhooks/toss")
public class TossWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody TossPaymentWebhookRequest request) {
        // PAYMENT_STATUS_CHANGED
        paymentWebhookService.handlePaymentStatusChanged(request);
        return ResponseEntity.ok().build();
    }
}
