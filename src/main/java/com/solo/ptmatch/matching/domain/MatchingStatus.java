package com.solo.ptmatch.matching.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchingStatus {
    PENDING("매칭 대기중"),
    ACCEPTED("매칭 수락됨"),
    REJECTED("매칭 거절됨"),
    COMPLETED("매칭 완료");

    private final String description;
}
