package com.solo.ptmatch.trainer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SportType {
    FITNESS("헬스/피트니스"),
    SWIMMING("수영"),
    GOLF("골프"),
    TENNIS("테니스"),
    RUNNING("러닝"),
    PILATES("필라테스"),
    YOGA("요가"),

    BOXING("복싱/격투기"),
    BADMINTON("배드민턴"),
    CLIMBING("클라이밍"),
    TABLE_TENNIS("탁구"),
    SOCCER("축구/풋살"),
    BASKETBALL("농구");

    private final String description;

    public static SportType fromDescription(String description) {
        for (SportType sportType : SportType.values()) {
            if (sportType.getDescription().equals(description)) {
                return sportType;
            }
        }
        return null;
    }
}
