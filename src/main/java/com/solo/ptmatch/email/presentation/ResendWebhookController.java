package com.solo.ptmatch.email.presentation;

import com.solo.ptmatch.email.application.ResendWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks/resend")
public class ResendWebhookController {

    private final ResendWebhookService resendWebhookService;

    @PostMapping("/emails")
    public ResponseEntity<Void> handleEmailWebhook(
            @RequestHeader("svix-id") String webhookId,
            @RequestHeader("svix-timestamp") String webhookTimestamp,
            @RequestHeader("svix-signature") String webhookSignature,
            @RequestBody String payload
    ) {
        if (!resendWebhookService.verify(payload, webhookId, webhookTimestamp, webhookSignature)) {
            return ResponseEntity.badRequest().build();
        }

        resendWebhookService.handleEvent(payload);
        return ResponseEntity.ok().build();
    }
}
