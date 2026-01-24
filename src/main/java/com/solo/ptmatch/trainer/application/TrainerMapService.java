package com.solo.ptmatch.trainer.application;

import com.google.common.geometry.S2CellId;
import com.solo.ptmatch.common.util.S2CellRange;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.S2ClusterResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerMapService {

    private final TrainerProfileRepository trainerProfileRepository;
    private final TrainerCellCacheService trainerCellCacheService;

    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getMapTrainers(double minLat, double maxLat, double minLon,
            double maxLon) {
        return getMapTrainersMarkers(minLat, maxLat, minLon, maxLon);
    }

    @Transactional(readOnly = true)
    public List<S2ClusterResponse> getMapClusters(double minLat, double maxLat, double minLon, double maxLon, int zoomLevel) {

        int clusterLevel = S2Util.zoomLevelToS2Level(zoomLevel);

        // 1. Level 14 Cell ID 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        // 2. count 캐시 MGET 조회
        Map<Long, Integer> cacheHits = trainerCellCacheService.getClusterCountsByCells(cellIdLongs);

        // 3. Cache Miss 처리
        List<Long> missCells = cellIdLongs.stream()
                .filter(id -> !cacheHits.containsKey(id))
                .toList();

        if (!missCells.isEmpty()) {
            // BETWEEN + Merge 최적화
            List<S2CellRange> ranges = S2Util.mergeCellIdsToRanges(missCells);
            Map<Long, Long> dbCounts = trainerProfileRepository.countTrainersByCellRanges(ranges);

            // count 캐싱
            for (Map.Entry<Long, Long> entry : dbCounts.entrySet()) {
                int count = entry.getValue().intValue();
                trainerCellCacheService.cacheCount(entry.getKey(), count);
                cacheHits.put(entry.getKey(), count);
            }

            // 빈 셀도 캐싱 (반복 조회 방지)
            for (Long missCell : missCells) {
                if (!cacheHits.containsKey(missCell)) {
                    trainerCellCacheService.cacheCount(missCell, 0);
                    cacheHits.put(missCell, 0);
                }
            }
        }

        // 4. 클러스터 레벨로 집계
        Map<Long, Integer> clusterMap = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : cacheHits.entrySet()) {
            S2CellId cellId = new S2CellId(entry.getKey());
            Long parentId = cellId.parent(clusterLevel).id();
            clusterMap.merge(parentId, entry.getValue(), Integer::sum);
        }

        return clusterMap.entrySet().stream()
                .map(e -> S2ClusterResponse.fromCellId(e.getKey(), e.getValue()))
                .toList();
    }

    // 지도 트레이너 탐색 (S2 Cell ID + Redis 캐시)
    // Redis MGET으로 배치 조회 → Cache Miss만 DB 조회 → 캐시 저장
    public List<TrainerSummaryResponse> getMapTrainersMarkers(double minLat, double maxLat, double minLon, double maxLon) {

        // 1. S2 Cell ID 리스트 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        // 2. Redis MGET 배치 조회
        Map<Long, List<TrainerSummaryResponse>> cacheHits = trainerCellCacheService.getTrainersByCells(cellIdLongs);

        // 3. Cache Miss 셀 ID 추출
        List<Long> cacheMissCellIds = cellIdLongs.stream()
                .filter(id -> !cacheHits.containsKey(id))
                .toList();

        // 4. Cache Miss 셀만 DB 조회
        if (!cacheMissCellIds.isEmpty()) {
            // 마커 조회는 셀 개수가 적기 때문에 BETWEEN와 IN 성능 차이 없음 -> IN 사용
            List<TrainerProfile> missedTrainers = trainerProfileRepository.findByS2CellIdIn(cacheMissCellIds);


            // 셀별로 그룹화하여 캐시 저장
            Map<Long, List<TrainerProfile>> missedTrainersByCell = missedTrainers.stream()
                    .collect(Collectors.groupingBy(TrainerProfile::getS2CellId));

            for (Map.Entry<Long, List<TrainerProfile>> entry : missedTrainersByCell.entrySet()) {
                List<TrainerSummaryResponse> responses = entry.getValue().stream()
                        .map(TrainerSummaryResponse::from)
                        .toList();
                trainerCellCacheService.cacheTrainersByCell(entry.getKey(), responses);
                cacheHits.put(entry.getKey(), responses);
            }

            // 빈 셀도 캐시 저장 (반복 조회 방지)
            // 저장하지 않으면 빈 셀은 항상 DB 조회 발생하기 때문
            for (Long missedCellId : cacheMissCellIds) {
                if (!cacheHits.containsKey(missedCellId)) {
                    trainerCellCacheService.cacheTrainersByCell(missedCellId, List.of());
                    cacheHits.put(missedCellId, List.of());
                }
            }
        }

        // 5. 모든 결과 병합
        return cacheHits.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}
