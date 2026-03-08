package com.solo.ptmatch.email.application;

import com.solo.ptmatch.email.domain.EmailOutbox;
import com.solo.ptmatch.email.infrastructure.EmailOutboxProperties;
import com.solo.ptmatch.email.infrastructure.EmailOutboxRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailOutboxService {

    private final EmailOutboxRepository emailOutboxRepository;
    private final EmailOutboxProperties emailOutboxProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createPaymentCompleted(String recipientEmail, String orderId, String customerName, Integer amount, LocalDateTime approvedAt) {

        EmailOutbox outbox = emailOutboxRepository.save(
                EmailOutbox.createPaymentComplete(
                        recipientEmail,
                        orderId,
                        customerName,
                        amount,
                        approvedAt,
                        emailOutboxProperties.getMaxRetry()));

        applicationEventPublisher.publishEvent(new EmailOutboxCreatedEvent(outbox.getId()));
    }

    //PENDING → PROCESSING
    @Transactional
    public EmailOutbox claimAndGet(Long outboxId) {
        int updated = emailOutboxRepository.markAsProcessing(outboxId);
        if (updated == 0) {
            return null;
        }
        return emailOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("이메일 Outbox를 찾을 수 없습니다. outboxId=" + outboxId));
    }

    //PROCESSING → SENT
    @Transactional
    public void markSent(Long outboxId) {
        EmailOutbox outbox = emailOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("이메일 Outbox를 찾을 수 없습니다. outboxId=" + outboxId));
        outbox.markSent();
    }

    //PROCESSING → PENDING or FAILED
    @Transactional
    public void markFailed(Long outboxId, String errorMessage) {
        EmailOutbox outbox = emailOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("이메일 Outbox를 찾을 수 없습니다. outboxId=" + outboxId));
        outbox.incrementRetry(errorMessage);
    }
}
