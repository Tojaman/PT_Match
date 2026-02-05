package com.solo.ptmatch.trainer.infrastructure.provider;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import com.solo.ptmatch.common.cache.facade.CacheFacade;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.CellStat;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.TrainerLatLon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TrainerMapProvider {

    private final CacheFacade cacheManagerFacade;
    private final TrainerProfileRepository trainerProfileRepository;

    public Map<Long, CellStat> getCounts(SportType sportType, List<Long> cellIds) {
        return getAllOrLoad(CacheTarget.TRAINER_COUNT, sportType, cellIds, this::loadCounts, CellStat::empty);
    }

    public Map<Long, List<TrainerLatLon>> getMarkers(SportType sportType, List<Long> cellIds) {
        return getAllOrLoad( CacheTarget.TRAINER_MARKER, sportType, cellIds, this::loadMarkers, List::of );
    }

    private Map<Long, CellStat> loadCounts(SportType sportType, List<Long> missIds) {
        return trainerProfileRepository.countTrainersByCellRanges(sportType, S2Util.mergeCellIdsToRanges(missIds));
    }

    private Map<Long, List<TrainerLatLon>> loadMarkers(SportType sportType, List<Long> missIds) {
        List<TrainerProfile> missedTrainers = trainerProfileRepository.findTrainersByCellRanges(sportType, S2Util.mergeCellIdsToRanges(missIds));
        // Cell ID별로 그룹화하여 좌표 리스트 생성
        return missedTrainers.stream()
                .collect(Collectors.groupingBy(
                        TrainerProfile::getS2CellId,
                        Collectors.mapping(TrainerLatLon::from, Collectors.toList())));
    }

    private <V> Map<Long, V> getAllOrLoad(
            CacheTarget cacheTarget,
            SportType sportType,
            List<Long> cellIds,
            BiFunction<SportType, List<Long>, Map<Long, V>> loader,
            Supplier<V> emptyValue
    ) {
        Map<Long, String> idToKey = new HashMap<>(cellIds.size());
        Map<Long, V> result = new HashMap<>(cellIds.size());
        List<Long> missIds = new ArrayList<>();

        // 1. 캐시 조회 및 미스 수집
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

        // 2. 캐시 미스 DB 조회 및 캐시 저장
        if (!missIds.isEmpty()) {
            // DB 조회 1회
            Map<Long, V> loaded = loader.apply(sportType, missIds);
            for (Long missId : missIds) {
                V value = loaded.get(missId);
                // 캐시 저장
                if (value == null) {
                    value = emptyValue.get();
                }
                result.put(missId, value);
                // 빈 값도 캐시 저장 - 캐시 관통(Cache Penetration) 방지 (불필요한 DB 조회 현상)
                if (value != null) {
                    cacheManagerFacade.put(cacheTarget, idToKey.get(missId), value);
                }
            }
        }

        return result;
    }
}
