package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerCellCacheService {

    private static final String CACHE_KEY_PREFIX = "trainer:cell:14:";
    private static final long TTL_HOURS = 24;

    private final RedisTemplate<String, List<TrainerSummaryResponse>> trainerCacheTemplate;

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

        String key = CACHE_KEY_PREFIX + cellId;
        trainerCacheTemplate.opsForValue().set(key, trainers, TTL_HOURS, TimeUnit.HOURS);
        log.info("✅ Cell {}에 대해 {}명의 트레이너 캐시 저장 완료", cellId, trainers.size());
    }

    // 캐시 무효화 (수정/삭제)
    public void invalidateCell(Long cellId) {
        if (cellId == null) {
            return;
        }

        String key = CACHE_KEY_PREFIX + cellId;
        trainerCacheTemplate.delete(key);
        log.debug("Cell {} 캐시 무효화 완료", cellId);
    }
}
