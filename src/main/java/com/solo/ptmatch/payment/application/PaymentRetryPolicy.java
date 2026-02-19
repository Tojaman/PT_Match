package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.payment.infrastructure.PaymentRetryProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRetryPolicy {

    private final PaymentRetryProperties paymentRetryProperties;

    public LocalDateTime firstRetryAt(LocalDateTime now) {
        return now.plus(backoffDelayForAttempt(1));
    }

    public LocalDateTime nextRetryAt(int attemptCount, LocalDateTime now) {
        return now.plus(backoffDelayForAttempt(attemptCount));
    }

    public LocalDateTime resolveDeadlineAt(LocalDateTime now) {
        return now.plus(paymentRetryProperties.resolveDeadline());
    }

    public boolean isManualReview(int nextAttemptCount, LocalDateTime resolveDeadlineAt, LocalDateTime now) {
        return nextAttemptCount >= paymentRetryProperties.maxAttempts()
                || (resolveDeadlineAt != null && now.isAfter(resolveDeadlineAt));
    }

    public int maxAttempts() {
        return paymentRetryProperties.maxAttempts();
    }

    public int reconcileBatchSize() {
        return paymentRetryProperties.reconcileBatchSize();
    }

    private Duration backoffDelayForAttempt(int attemptCount) {
        List<Duration> backoffDelays = paymentRetryProperties.backoffDelays();
        int index = Math.max(0, Math.min(attemptCount - 1, backoffDelays.size() - 1));
        return backoffDelays.get(index);
    }
}
