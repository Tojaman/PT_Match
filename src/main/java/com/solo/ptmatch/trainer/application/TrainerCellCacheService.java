package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerCellCacheService {

    private static final String CACHE_KEY_PREFIX = "trainer:cell:14:";
    private static final String COUNT_KEY_PREFIX = "trainer:cell:count:14:";
    private static final long TTL_HOURS = 24;

    private final RedisTemplate<String, List<TrainerSummaryResponse>> trainerCacheTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // 캐시 조회
    public Map<Long, List<TrainerSummaryResponse>> getTrainersByCells(List<Long> cellIds) {
        if (cellIds == null || cellIds.isEmpty()) {
            return Map.of();
        }

        // 1. Redis 키 리스트 생성
        List<String> keys = cellIds.stream()
                .map(id -> CACHE_KEY_PREFIX + id)
                .toList();

        // 2. MGET 배치 조회
        List<List<TrainerSummaryResponse>> cachedValues = trainerCacheTemplate.opsForValue().multiGet(keys);

        // 3. 결과 매핑
        Map<Long, List<TrainerSummaryResponse>> result = new HashMap<>();
        if (cachedValues != null) {
            for (int i = 0; i < cellIds.size(); i++) {
                List<TrainerSummaryResponse> trainers = cachedValues.get(i);
                if (trainers != null) { // empth 캐시 존재
                    result.put(cellIds.get(i), trainers);
                }
            }
        }

        return result;
    }

    // 캐시 저장
    public void cacheTrainersByCell(Long cellId, List<TrainerSummaryResponse> trainers) {
        if (cellId == null || trainers == null) {
            return;
        }

        String dataKey = CACHE_KEY_PREFIX + cellId;
        String countKey = COUNT_KEY_PREFIX + cellId;

        try {
            trainerCacheTemplate.opsForValue().set(dataKey, trainers, TTL_HOURS, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(trainers.size()), TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            trainerCacheTemplate.delete(dataKey);
            stringRedisTemplate.delete(countKey);
            // 예외 전파 방지
            log.warn("캐시 저장 실패 (cellId: {}): {}", cellId, e.getMessage());
        }
    }

    // 캐시 무효화 (수정/삭제)
    public void invalidateCell(Long cellId) {
        if (cellId == null) {
            return;
        }

        String dataKey = CACHE_KEY_PREFIX + cellId;
        String countKey = COUNT_KEY_PREFIX + cellId;

        try {
            trainerCacheTemplate.delete(dataKey);
            stringRedisTemplate.delete(countKey);
        } catch (Exception e) {
            // 예외 전파 방지
            log.warn("캐시 삭제 실패 (cellId: {}): {}", cellId, e.getMessage());
        }
    }

    // 클러스터 조회 
    public Map<Long, Integer> getClusterCountsByCells(List<Long> cellIds) {
        if (cellIds == null || cellIds.isEmpty()) {
            return Map.of();
        }

        List<String> keys = cellIds.stream()
                .map(id -> COUNT_KEY_PREFIX + id)
                .toList();

        List<String> counts = stringRedisTemplate.opsForValue().multiGet(keys);

        Map<Long, Integer> result = new HashMap<>();
        if (counts != null) {
            for (int i = 0; i < cellIds.size(); i++) {
                if (counts.get(i) != null) {
                    result.put(cellIds.get(i), Integer.parseInt(counts.get(i)));
                }
            }
        }
        return result;
    }

    // 클러스터 저장
    public void cacheCount(Long cellId, int count) {
        if (cellId == null) {
            return;
        }

        String key = COUNT_KEY_PREFIX + cellId;
        stringRedisTemplate.opsForValue().set(
                key,
                String.valueOf(count),
                TTL_HOURS,
                TimeUnit.HOURS);
    }
}
