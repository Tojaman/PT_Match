package com.solo.ptmatch.email.presentation.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResendWebhookRequest(
        String type,
        @JsonProperty("created_at") String createdAt,
        ResendWebhookDataRequest data
) {
}
