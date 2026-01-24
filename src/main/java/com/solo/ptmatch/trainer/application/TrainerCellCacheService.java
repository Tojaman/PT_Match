package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.domain.SportType;
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

    // 캐시 키 형식: trainer:cell:{sportType}:{s2CellId}
    private static final String CACHE_KEY_FORMAT = "trainer:cell:%s:14:%d";
    private static final String COUNT_KEY_FORMAT = "trainer:cell:count:%s:14:%d";
    private static final long TTL_HOURS = 24;

    private final RedisTemplate<String, List<TrainerSummaryResponse>> trainerCacheTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // 캐시 조회 (종목별)
    public Map<Long, List<TrainerSummaryResponse>> getTrainersByCells(SportType sportType, List<Long> cellIds) {
        if (sportType == null || cellIds == null || cellIds.isEmpty()) {
            return Map.of();
        }

        // 1. Redis 키 리스트 생성 (종목별)
        List<String> keys = cellIds.stream()
                .map(id -> String.format(CACHE_KEY_FORMAT, sportType.name(), id))
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

    // 캐시 저장 (종목별)
    public void cacheTrainersByCell(SportType sportType, Long cellId, List<TrainerSummaryResponse> trainers) {
        if (sportType == null || cellId == null || trainers == null) {
            return;
        }

        String dataKey = String.format(CACHE_KEY_FORMAT, sportType.name(), cellId);
        String countKey = String.format(COUNT_KEY_FORMAT, sportType.name(), cellId);

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

    // 캐시 무효화 (수정/삭제, 종목별)
    public void invalidateCell(SportType sportType, Long cellId) {
        if (sportType == null || cellId == null) {
            return;
        }

        String dataKey = String.format(CACHE_KEY_FORMAT, sportType.name(), cellId);
        String countKey = String.format(COUNT_KEY_FORMAT, sportType.name(), cellId);

        try {
            trainerCacheTemplate.delete(dataKey);
            stringRedisTemplate.delete(countKey);
        } catch (Exception e) {
            // 예외 전파 방지
            log.warn("캐시 삭제 실패 (cellId: {}): {}", cellId, e.getMessage());
        }
    }

    // 클러스터 조회 (종목별)
    public Map<Long, Integer> getClusterCountsByCells(SportType sportType, List<Long> cellIds) {
        if (sportType == null || cellIds == null || cellIds.isEmpty()) {
            return Map.of();
        }

        List<String> keys = cellIds.stream()
                .map(id -> String.format(COUNT_KEY_FORMAT, sportType.name(), id))
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

    // 클러스터 저장 (종목별)
    public void cacheCount(SportType sportType, Long cellId, int count) {
        if (sportType == null || cellId == null) {
            return;
        }

        String key = String.format(COUNT_KEY_FORMAT, sportType.name(), cellId);
        stringRedisTemplate.opsForValue().set(
                key,
                String.valueOf(count),
                TTL_HOURS,
                TimeUnit.HOURS);
    }
}
