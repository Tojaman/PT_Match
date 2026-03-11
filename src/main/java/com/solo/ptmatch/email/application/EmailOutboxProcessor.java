package com.solo.ptmatch.email.application;

import com.solo.ptmatch.email.domain.EmailOutbox;
import com.solo.ptmatch.email.infrastructure.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOutboxProcessor {

    private final EmailOutboxService emailOutboxService;
    private final EmailSender emailSender;

    public void processOne(Long outboxId) {
        // TX1: 원자적 선점 + 조회
        EmailOutbox outbox = emailOutboxService.claimAndGet(outboxId);
        if (outbox == null) {
            log.debug("이메일 Outbox 선점 실패 (이미 처리 중). outboxId={}", outboxId);
            return;
        }

        // 트랜잭션 없이 이메일 발송
        try {
            emailSender.send(
                    outbox.getRecipientEmail(),
                    outbox.getSubject(),
                    outbox.getBody(),
                    outbox.getReferenceId());
            // TX2 성공: PROCESSING → SENT
            emailOutboxService.markSent(outbox.getId());
        } catch (Exception e) {
            log.warn("이메일 발송 실패. outboxId={}, retryCount={}/{}. error={}",
                    outbox.getId(), outbox.getRetryCount(), outbox.getMaxRetry(), e.getMessage());
            // TX2 실패: PROCESSING → PENDING or FAILED
            emailOutboxService.handleSendFailure(outbox.getId(), e.getMessage());
        }
    }
}
