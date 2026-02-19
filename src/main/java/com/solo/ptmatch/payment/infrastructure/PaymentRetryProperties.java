package com.solo.ptmatch.payment.infrastructure;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "payment.retry")
public record PaymentRetryProperties(
        @NotEmpty List<Duration> backoffDelays,
        @Positive int maxAttempts,
        @NotNull Duration resolveDeadline,
        @NotNull Duration reconcileInterval,
        @Positive int reconcileBatchSize
) {
}
