package com.solo.ptmatch.email.scheduler;

import com.solo.ptmatch.email.application.EmailOutboxProcessor;
import com.solo.ptmatch.email.domain.EmailOutbox;
import com.solo.ptmatch.email.domain.EmailOutboxStatus;
import com.solo.ptmatch.email.infrastructure.EmailOutboxProperties;
import com.solo.ptmatch.email.infrastructure.EmailOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailOutboxScheduler {

    private final EmailOutboxRepository emailOutboxRepository;
    private final EmailOutboxProcessor emailOutboxProcessor;
    private final EmailOutboxProperties emailOutboxProperties;

    @Scheduled(fixedDelayString = "${email.outbox.poll-interval:5000}")
    public void pollAndSend() {
        LocalDateTime now = LocalDateTime.now();

        List<EmailOutbox> pendingList = emailOutboxRepository.findPendingEmails(
                EmailOutboxStatus.PENDING,
                now,
                PageRequest.of(0, emailOutboxProperties.getBatchSize()));

        for (EmailOutbox outbox : pendingList) {
            try {
                emailOutboxProcessor.processOne(outbox.getId());
            } catch (Exception e) {
                log.error("이메일 Outbox 처리 중 예외. outboxId={}", outbox.getId(), e);
            }
        }
    }
}
