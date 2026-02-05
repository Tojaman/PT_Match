package com.solo.ptmatch.common.cache.enums;

import com.solo.ptmatch.trainer.domain.SportType;

public enum CacheTarget {
    TRAINER_COUNT("trainerCount", CacheType.LOCAL, 3_600, 200_000),
    TRAINER_MARKER("trainerMarker", CacheType.LOCAL, 3_600, 100_000);

    private final String name;
    private final CacheType type;
    private final long ttlSeconds;
    private final long maxEntries;

    CacheTarget(String name, CacheType type, long ttlSeconds, long maxEntries) {
        this.name = name;
        this.type = type;
        this.ttlSeconds = ttlSeconds;
        this.maxEntries = maxEntries;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public long getMaxEntries() {
        return maxEntries;
    }

    public String buildKey(SportType sportType, long cellId) {
        return sportType.name() + ":" + cellId;
    }
}
