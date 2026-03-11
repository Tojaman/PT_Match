package com.solo.ptmatch.email.presentation.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResendWebhookDataRequest(
        @JsonProperty("email_id") String emailId,
        Map<String, String> tags,
        String message,
        ResendWebhookErrorRequest failed,
        ResendWebhookBounceRequest bounce
) {
}
