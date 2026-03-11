package com.solo.ptmatch.email.presentation.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResendWebhookErrorRequest(
        String reason
) {
}
