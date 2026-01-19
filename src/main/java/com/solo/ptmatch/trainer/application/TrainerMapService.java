package com.solo.ptmatch.trainer.application;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.solo.ptmatch.common.util.S2CellRange;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.GridClusterProjection;
import com.solo.ptmatch.trainer.infrastructure.LatLngProjection;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.GridClusterResponse;
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

    // 지도 트레이너 탐색 (Bounding Box 기반)
    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getMapTrainersBbox(double minLat, double maxLat, double minLon, double maxLon) {
        return trainerProfileRepository
                .findByGymLatitudeBetweenAndGymLongitudeBetween(minLat, maxLat, minLon, maxLon)
                .stream()
                .map(TrainerSummaryResponse::from)
                .toList();
    }

    // 지도 트레이너 탐색 (S2 Cell ID IN 쿼리 기반)
    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getMapTrainersS2(double minLat, double maxLat, double minLon, double maxLon) {
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon,
                S2Util.STORAGE_LEVEL);

        List<Long> cellIdLongs = cellIds.stream()
                .map(S2CellId::id)
                .toList();

        return trainerProfileRepository.findByS2CellIdIn(cellIdLongs)
                .stream()
                .map(TrainerSummaryResponse::from)
                .toList();
    }

    // ST_SnapToGrid 기반 클러스터링 (BETWEEN 조건)
    @Transactional(readOnly = true)
    public List<GridClusterResponse> getMapClustersSnapToGridBetween(double minLat, double maxLat, double minLon,
            double maxLon, int zoomLevel) {
        double gridSize = 0.0005 * Math.pow(2, zoomLevel - 1);

        List<GridClusterProjection> clusters = trainerProfileRepository.findSnapToGridClustersBetween(
                minLon, minLat, maxLon, maxLat, gridSize);

        return clusters.stream()
                .map(GridClusterResponse::from)
                .toList();
    }

    // S2 Geometry 기반 클러스터링
    @Transactional(readOnly = true)
    public List<S2ClusterResponse> getMapClustersS2Java(double minLat, double maxLat, double minLon, double maxLon,
            int zoomLevel) {
        int cellLevel = S2Util.zoomLevelToS2Level(zoomLevel);
        List<S2CellRange> ranges = S2Util.getCoveringRanges(minLat, maxLat, minLon, maxLon, cellLevel);
        List<LatLngProjection> locations = trainerProfileRepository.findLatLngByS2CellRangesCriteria(ranges);

        Map<Long, Integer> clusterMap = new HashMap<>();
        for (LatLngProjection loc : locations) {
            S2LatLng latLng = S2LatLng.fromDegrees(loc.gymLatitude(), loc.gymLongitude());
            S2CellId parentCell = S2CellId.fromLatLng(latLng).parent(cellLevel);
            clusterMap.merge(parentCell.id(), 1, Integer::sum);
        }

        return clusterMap.entrySet().stream()
                .map(entry -> S2ClusterResponse.fromCellId(entry.getKey(), entry.getValue()))
                .toList();
    }

    // S2 Geometry 기반 클러스터링 (14레벨로 range scan 최적화)
    @Transactional(readOnly = true)
    public List<S2ClusterResponse> getMapClustersS2JavaMerge(double minLat, double maxLat, double minLon, double maxLon,
            int zoomLevel) {
        int cellLevel = S2Util.zoomLevelToS2Level(zoomLevel);
        List<S2CellRange> rangesMerge = S2Util.getCoveringRangesMerge(minLat, maxLat, minLon, maxLon);
        List<LatLngProjection> locations = trainerProfileRepository.findLatLngByS2CellRangesCriteria(rangesMerge);

        Map<Long, Integer> clusterMap = new HashMap<>();
        for (LatLngProjection loc : locations) {
            S2LatLng latLng = S2LatLng.fromDegrees(loc.gymLatitude(), loc.gymLongitude());
            S2CellId parentCell = S2CellId.fromLatLng(latLng).parent(cellLevel);
            clusterMap.merge(parentCell.id(), 1, Integer::sum);
        }

        return clusterMap.entrySet().stream()
                .map(entry -> S2ClusterResponse.fromCellId(entry.getKey(), entry.getValue()))
                .toList();
    }

    // ==================== Redis 캐시 적용 API ====================
    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getMapTrainersMarkers(double minLat, double maxLat, double minLon,
            double maxLon) {
        return getMapTrainersS2Cached(minLat, maxLat, minLon, maxLon);
    }

    // S2 Geometry 기반 클러스터 조회 (Redis 캐시 적용)
    // 마커 조회 캐시를 재사용하여 Java에서 집계
    @Transactional(readOnly = true)
    public List<S2ClusterResponse> getMapClustersS2Cached(double minLat, double maxLat, double minLon, double maxLon,
            int zoomLevel) {
        int clusterLevel = S2Util.zoomLevelToS2Level(zoomLevel);

        // 1. 캐시 적용 마커 조회 재사용 -> GC 폭증
        List<TrainerSummaryResponse> trainers = getMapTrainersS2Cached(minLat, maxLat, minLon, maxLon);

        // 2. 줌에 맞는 Cell 레벨로 집계
        Map<Long, Long> clusterMap = trainers.stream()
                .collect(Collectors.groupingBy(
                        trainer -> {
                            S2LatLng latLng = S2LatLng.fromDegrees(trainer.gymLatitude(), trainer.gymLongitude());
                            return S2CellId.fromLatLng(latLng).parent(clusterLevel).id();
                        },
                        Collectors.counting()));

        return clusterMap.entrySet().stream()
                .map(entry -> S2ClusterResponse.fromCellId(entry.getKey(), entry.getValue().intValue()))
                .toList();
    }

    // 지도 트레이너 탐색 (S2 Cell ID + Redis 캐시)
    // Redis MGET으로 배치 조회 → Cache Miss만 DB 조회 → 캐시 저장
    public List<TrainerSummaryResponse> getMapTrainersS2Cached(double minLat, double maxLat, double minLon,
            double maxLon) {

        // 1. S2 Cell ID 리스트 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        // 2. Redis MGET 배치 조회
        Map<Long, List<TrainerSummaryResponse>> cacheHits = trainerCellCacheService.getTrainersByCells(cellIdLongs);

        // 3. Cache Miss 셀 ID 추출
        List<Long> cacheMissCellIds = cellIdLongs.stream()
                .filter(id -> !cacheHits.containsKey(id))
                .toList();

        log.info("캐시 미스 크기: {}", cacheMissCellIds.size());

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
