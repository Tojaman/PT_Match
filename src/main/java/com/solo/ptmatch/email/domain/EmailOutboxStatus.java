package com.solo.ptmatch.email.domain;

public enum EmailOutboxStatus {
    PENDING,   // 발송 대기
    SENT,      // 발송 완료
    FAILED     // 최대 재시도 초과 → 발송 실패
}
