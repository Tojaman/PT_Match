package com.solo.ptmatch.common.cache.enums;

import com.solo.ptmatch.trainer.domain.SportType;

public enum CacheTarget {
    TRAINER_COUNT("trainerCount", CacheType.LOCAL, 3_600),
    TRAINER_MARKER("trainerMarker", CacheType.LOCAL, 3_600);

    private final String name;
    private final CacheType type;
    private final long ttlSeconds;

    CacheTarget(String name, CacheType type, long ttlSeconds) {
        this.name = name;
        this.type = type;
        this.ttlSeconds = ttlSeconds;
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

    public String buildKey(SportType sportType, long cellId) {
        return sportType.name() + ":" + cellId;
    }
}
