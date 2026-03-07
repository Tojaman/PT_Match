package com.solo.ptmatch.email.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailOutboxEventListener {

    private final EmailOutboxProcessor emailOutboxProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailOutboxCreated(EmailOutboxCreatedEvent event) {
        emailOutboxProcessor.processOne(event.outboxId());
    }
}
