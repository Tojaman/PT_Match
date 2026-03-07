package com.solo.ptmatch.email.application;

import com.solo.ptmatch.email.domain.EmailOutbox;
import com.solo.ptmatch.email.domain.EmailOutboxStatus;
import com.solo.ptmatch.email.infrastructure.EmailOutboxRepository;
import com.solo.ptmatch.email.infrastructure.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOutboxProcessor {

    private final EmailOutboxRepository emailOutboxRepository;
    private final EmailSender emailSender;

    @Transactional
    public void processOne(Long outboxId) {
        EmailOutbox outbox = emailOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("이메일 Outbox를 찾을 수 없습니다. outboxId=" + outboxId));

        try {
            emailSender.send(
                    outbox.getRecipientEmail(),
                    outbox.getSubject(),
                    outbox.getBody(),
                    outbox.getReferenceId());

            outbox.markSent();
        } catch (Exception e) {
            outbox.incrementRetry(e.getMessage());
            log.warn("이메일 발송 실패. outboxId={}, retryCount={}/{}. error={}",
                    outbox.getId(), outbox.getRetryCount(),
                    outbox.getMaxRetry(), e.getMessage());
        }
    }
}
