package com.solo.ptmatch.payment.domain;

public enum PaymentStatus {
    READY,
    APPROVING,
    DONE,
    FAILED,
    UNKNOWN,
    CANCEL_PENDING,
    CANCELED,
    EXPIRED,
    MANUAL_REVIEW
}
