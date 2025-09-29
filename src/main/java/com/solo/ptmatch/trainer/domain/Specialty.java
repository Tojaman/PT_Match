package com.solo.ptmatch.trainer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Specialty {
    WEIGHT_LOSS("다이어트"),
    REHABILITATION("재활"),
    STRENGTH_CONDITIONING("근력 강화"),
    POSTURE_CORRECTION("체형 교정"),
    FLEXIBILITY("유연성 향상"),
    BODYBUILDING("바디빌딩"),
    NUTRITION_COACHING("영양 코칭"),
    PREGNANCY_CARE("산전·산후 케어"),
    SENIOR_FITNESS("시니어 피트니스"),
    SPORTS_PERFORMANCE("스포츠 퍼포먼스")
;

    private final String description;
}
