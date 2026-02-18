package com.solo.ptmatch.payment.infrastructure.toss;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentsProperties(
        @NotBlank String baseUrl,
        @NotBlank String clientKey,
        @NotBlank String secretKey
) {
}
