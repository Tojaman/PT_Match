package com.solo.ptmatch.payment.infrastructure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
        @NotNull Duration orderTtl,
        @NotBlank String successUrl,
        @NotBlank String failUrl
) {
}
