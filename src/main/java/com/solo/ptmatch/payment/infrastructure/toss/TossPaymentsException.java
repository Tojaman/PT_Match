package com.solo.ptmatch.payment.infrastructure.toss;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class TossPaymentsException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String providerCode;
    private final String providerMessage;

    public TossPaymentsException(HttpStatusCode statusCode, String providerCode, String providerMessage) {
        super(providerMessage);
        this.statusCode = statusCode;
        this.providerCode = providerCode;
        this.providerMessage = providerMessage;
    }

    public boolean isClientError() {
        return statusCode != null && statusCode.is4xxClientError();
    }
}
