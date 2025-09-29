package com.solo.ptmatch.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    WEIGHT_LOSS("다이어트 집중"),
    MUSCLE_GAIN("근성장 프로그램"),
    REHABILITATION("재활 트레이닝"),
    BODY_PROFILE("바디프로필 준비"),
    PILATES("필라테스·코어"),
    HOME_TRAINING("홈트 전용"),
    ONLINE_COACHING("온라인 코칭"),
    NUTRITION_PACKAGE("영양 관리 패키지")
;

    private final String description;
}
