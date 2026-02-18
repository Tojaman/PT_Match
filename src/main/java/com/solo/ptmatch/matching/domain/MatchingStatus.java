package com.solo.ptmatch.matching.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchingStatus {
    PAYMENT_PENDING("결제 대기중"),
    PENDING("매칭 대기중"),
    ACCEPTED("매칭 수락됨"),
    REJECTED("매칭 거절됨"),
    COMPLETED("매칭 완료"),
    CANCELED("매칭 취소됨");

    private final String description;
}
