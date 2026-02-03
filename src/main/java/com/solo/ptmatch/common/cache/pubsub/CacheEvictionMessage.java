package com.solo.ptmatch.common.cache.pubsub;

import com.solo.ptmatch.common.cache.enums.CacheTarget;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEvictionMessage {
    private CacheTarget cacheTarget;
    private String key;
}