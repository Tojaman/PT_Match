package com.solo.ptmatch.trainer.application;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.solo.ptmatch.common.util.S2CellRange;
import com.solo.ptmatch.common.util.S2Util;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerMapService {

    private final TrainerProfileRepository trainerProfileRepository;

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
}
