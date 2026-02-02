package com.solo.ptmatch.common.cache.enums;

import com.solo.ptmatch.trainer.presentation.response.TrainerLatLonList;

public enum CacheTarget {
    TRAINER_COUNT("trainerCount", CacheType.LOCAL, 3_600, Integer.class, "trainerCount:%s:%s"),
    TRAINER_MARKER("trainerMarker", CacheType.LOCAL, 3_600, TrainerLatLonList.class, "trainerMarker:%s:%s");

    private final String name;
    private final CacheType type;
    private final long ttlSeconds;
    private final Class<?> valueType;
    private final String keyPattern;

    CacheTarget(String name, CacheType type, long ttlSeconds, Class<?> valueType, String keyPattern) {
        this.name = name;
        this.type = type;
        this.ttlSeconds = ttlSeconds;
        this.valueType = valueType;
        this.keyPattern = keyPattern;
    }

    public String getName() {
        return name;
    }

    public CacheType getType() {
        return type;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public String buildKey(Object... args) {
        return String.format(keyPattern, args);
    }
}
