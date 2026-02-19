package com.solo.ptmatch.payment.infrastructure.toss;

import org.springframework.http.HttpStatusCode;

public class TossServerException extends TossPaymentsException {

    public TossServerException(HttpStatusCode statusCode, String providerCode, String providerMessage) {
        super(statusCode, providerCode, providerMessage);
    }
}
