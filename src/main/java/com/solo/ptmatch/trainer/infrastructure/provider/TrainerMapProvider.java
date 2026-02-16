package com.solo.ptmatch.trainer.infrastructure.provider;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import com.solo.ptmatch.common.cache.facade.CacheFacade;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.CellStat;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.TrainerMarker;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerMapProvider {

    private final CacheFacade cacheManagerFacade;
    private final TrainerProfileRepository trainerProfileRepository;

    // Cell별 락 관리 (Cache Stampede 방지)
    private final ConcurrentHashMap<String, ReentrantLock> cellLocks = new ConcurrentHashMap<>();

    public Map<Long, CellStat> getCounts(SportType sportType, List<Long> cellIds) {
        return getAllOrLoad(CacheTarget.TRAINER_COUNT, sportType, cellIds, this::loadCounts, CellStat::empty);
    }

    public Map<Long, List<TrainerMarker>> getMarkers(SportType sportType, List<Long> cellIds) {
        return getAllOrLoad(CacheTarget.TRAINER_MARKER, sportType, cellIds, this::loadMarkers, List::of);
    }

    private Map<Long, CellStat> loadCounts(SportType sportType, List<Long> missIds) {
        return trainerProfileRepository.countTrainersByCellRanges(sportType, S2Util.mergeCellIdsToRanges(missIds));
    }

    private Map<Long, List<TrainerMarker>> loadMarkers(SportType sportType, List<Long> missIds) {
        List<TrainerProfile> missedTrainers = trainerProfileRepository.findTrainersByCellRanges(sportType,
                S2Util.mergeCellIdsToRanges(missIds));
        // Cell ID별로 그룹화하여 좌표 리스트 생성
        return missedTrainers.stream()
                .collect(Collectors.groupingBy(
                        TrainerProfile::getS2CellId,
                        Collectors.mapping(TrainerMarker::from, Collectors.toList())));
    }

    public List<TrainerSummaryResponse> getTrainerSummaries(List<Long> trainerIds) {
        List<TrainerProfile> profiles = trainerProfileRepository.findByIdIn(trainerIds);
        Map<Long, TrainerSummaryResponse> summaryById = profiles.stream()
                .map(TrainerSummaryResponse::from)
                .collect(Collectors.toMap(TrainerSummaryResponse::trainerId, response -> response));

        return trainerIds.stream()
                .map(summaryById::get)
                .filter(summary -> summary != null)
                .toList();
    }

    public List<TrainerSummaryResponse> getTrainerSummariesByClusterCellCursor(SportType sportType, long s2CellId,
            long cursor, int limit) {
        return trainerProfileRepository.findTrainersByClusterCellCursor(sportType, s2CellId, cursor, limit).stream()
                .map(TrainerSummaryResponse::from)
                .toList();
    }

    /**
     * Cell별 뮤텍스 락을 이용한 Cache Stampede 방지 패턴
     * 1) Lock-free 캐시 조회
     * 2) Miss된 cell에 대해 tryLock으로 로딩/대기 그룹 분리
     * 3) 더블 체크 후 배치 DB 조회
     * 4) 락 대기 후 캐시 재조회 (로딩 완료 신호 대기)
     * 5) Fallback: 로딩 실패 시 마지막 배치 로드
     */
    private <V> Map<Long, V> getAllOrLoad(
            CacheTarget cacheTarget,
            SportType sportType,
            List<Long> cellIds,
            BiFunction<SportType, List<Long>, Map<Long, V>> loader,
            Supplier<V> emptyValue) {
        Map<Long, String> idToKey = new HashMap<>(cellIds.size());
        Map<Long, V> result = new HashMap<>(cellIds.size());
        List<Long> missIds = new ArrayList<>();

        // 1) 1차 캐시 조회 (lock-free)
        for (Long id : cellIds) {
            String key = cacheTarget.buildKey(sportType, id);
            idToKey.put(id, key);

            V cached = cacheManagerFacade.get(cacheTarget, key);
            if (cached != null) {
                result.put(id, cached);
            } else {
                missIds.add(id);
            }
        }

        // ALL Cache Hit
        if (missIds.isEmpty()) {
            return result;
        }

        List<Long> acquiredIds = new ArrayList<>();
        List<Long> waitIds = new ArrayList<>();

        // 2) cell별 tryLock: 로딩 담당 그룹과 대기 그룹 분리
        for (Long missId : missIds) {
            String key = idToKey.get(missId);
            ReentrantLock lock = cellLocks.computeIfAbsent(key, k -> new ReentrantLock());
            if (lock.tryLock()) {
                acquiredIds.add(missId); // DB 조회할 셀
            } else {
                waitIds.add(missId); // 다른 스레드가 락 획득한 셀
            }
        }

        // 3) 락 획득 그룹: 더블 체크 후 배치 DB 조회
        try {
            List<Long> toLoadIds = new ArrayList<>();

            // 더블 체크: miss 확인 후 락 획득 전 다른 스레드가 락 획득 -> 로딩/캐싱 -> 락 해제했을 수 있음 (DB 중복 조회 방지)
            for (Long acquiredId : acquiredIds) {
                String key = idToKey.get(acquiredId);
                V cached = cacheManagerFacade.get(cacheTarget, key);
                if (cached != null) {
                    result.put(acquiredId, cached);
                } else {
                    toLoadIds.add(acquiredId);
                }
            }

            // 여전히 miss인 cell만 배치 DB 조회 1회
            if (!toLoadIds.isEmpty()) {
                Map<Long, V> loaded = loader.apply(sportType, toLoadIds);
                putLoadedValues(cacheTarget, idToKey, result, toLoadIds, loaded, emptyValue);
            }
        } finally {
            unlockAll(idToKey, acquiredIds);
        }

        // 4) 락 대기 그룹: 락 획득 후 캐시 재조회
        List<Long> unresolvedIds = new ArrayList<>();
        for (Long waitId : waitIds) {
            String key = idToKey.get(waitId);
            ReentrantLock lock = cellLocks.get(key);
            lock.lock();
            try {
                V cached = cacheManagerFacade.get(cacheTarget, key);
                if (cached != null) {
                    result.put(waitId, cached);
                } else {
                    // 로딩 스레드 예외/중단 케이스 대비
                    unresolvedIds.add(waitId);
                }
            } finally {
                lock.unlock();
            }
        }

        // 5) 비정상 fallback: 로딩 스레드 실패 시 마지막 배치 로드
        if (!unresolvedIds.isEmpty()) {
            Map<Long, V> loaded = loader.apply(sportType, unresolvedIds);
            putLoadedValues(cacheTarget, idToKey, result, unresolvedIds, loaded, emptyValue);
        }

        return result;
    }

    // 캐시 저장 (3단계, 5단계에서 재사용)
    private <V> void putLoadedValues(
            CacheTarget cacheTarget,
            Map<Long, String> idToKey,
            Map<Long, V> result,
            List<Long> targetIds,
            Map<Long, V> loaded,
            Supplier<V> emptyValue) {
        for (Long id : targetIds) {
            V value = loaded.get(id);
            if (value == null) {
                value = emptyValue.get();
            }
            result.put(id, value);
            // 빈 값도 캐시 저장 - Cache Penetration 방지
            if (value != null) {
                cacheManagerFacade.put(cacheTarget, idToKey.get(id), value);
            }
        }
    }

    private void unlockAll(Map<Long, String> idToKey, List<Long> ids) {
        for (int i = ids.size() - 1; i >= 0; i--) {
            String key = idToKey.get(ids.get(i));
            ReentrantLock lock = cellLocks.get(key);
            if (lock != null) {
                lock.unlock();
            }
        }
    }
}
