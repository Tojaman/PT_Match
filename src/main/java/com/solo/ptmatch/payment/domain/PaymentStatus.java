package com.solo.ptmatch.payment.domain;

public enum PaymentStatus {
    READY,
    APPROVING,
    DONE,
    FAILED,
    UNKNOWN,
    CANCELED,
    EXPIRED,
    MANUAL_REVIEW
}
