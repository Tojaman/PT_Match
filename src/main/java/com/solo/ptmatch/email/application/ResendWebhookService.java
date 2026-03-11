package com.solo.ptmatch.email.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svix.Webhook;
import com.solo.ptmatch.email.domain.EmailOutbox;
import com.solo.ptmatch.email.infrastructure.ResendProperties;
import com.solo.ptmatch.email.presentation.request.ResendWebhookBounceRequest;
import com.solo.ptmatch.email.presentation.request.ResendWebhookDataRequest;
import com.solo.ptmatch.email.presentation.request.ResendWebhookErrorRequest;
import com.solo.ptmatch.email.presentation.request.ResendWebhookRequest;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResendWebhookService {

    private final ObjectMapper objectMapper;
    private final EmailOutboxService emailOutboxService;
    private final ResendProperties resendProperties;

    @Transactional
    public void handleEvent(String payload) {
        ResendWebhookRequest webhook = readPayload(payload);
        String eventType = webhook.type();
        ResendWebhookDataRequest data = webhook.data();

        String referenceId = data.tags().get("reference_id");
        String emailId = data.emailId();

        EmailOutbox outbox = emailOutboxService.findByReferenceId(referenceId)
                .orElse(null);
        if (outbox == null) {
            log.warn("reference_id에 해당하는 Email Outbox가 없습니다. referenceId={}, eventType={}, emailId={}", referenceId, eventType, emailId);
            return;
        }

        switch (eventType) {
            case "email.delivered" -> outbox.markDelivered();
            case "email.failed" -> outbox.markFailed();
            case "email.bounced" -> outbox.markFailed();
            default -> log.debug("처리 대상이 아닌 Resend 웹훅 이벤트를 무시합니다. referenceId={}, eventType={}, emailId={}", referenceId, eventType, emailId);
        }
    }

    public boolean verify(String payload, String webhookId, String webhookTimestamp, String webhookSignature) {
        try {
            Webhook webhook = new Webhook(resendProperties.getWebhookSecret());
            HttpHeaders headers = HttpHeaders.of(
                    Map.of(
                            "svix-id", List.of(webhookId),
                            "svix-timestamp", List.of(webhookTimestamp),
                            "svix-signature", List.of(webhookSignature)),
                    (name, value) -> true);
            webhook.verify(payload, headers);
            return true;
        } catch (Exception exception) {
            log.warn("Resend 웹훅 서명 검증 실패. webhookId={}, webhookTimestamp={}", webhookId, webhookTimestamp);
            return false;
        }
    }

    private ResendWebhookRequest readPayload(String payload) {
        try {
            return objectMapper.readValue(payload, ResendWebhookRequest.class);
        } catch (IOException exception) {
            log.warn("Resend 웹훅 payload 파싱에 실패했습니다. payload={}", payload);
            return null;
        }
    }
}
