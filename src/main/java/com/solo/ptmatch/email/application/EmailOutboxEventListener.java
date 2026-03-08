package com.solo.ptmatch.email.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EmailOutboxEventListener {

    private final EmailOutboxProcessor emailOutboxProcessor;

    @Async("emailOutboxExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailOutboxCreated(EmailOutboxCreatedEvent event) {
        emailOutboxProcessor.processOne(event.outboxId());
    }
}
