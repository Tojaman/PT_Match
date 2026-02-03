package com.solo.ptmatch.trainer.application;

import com.google.common.geometry.S2CellId;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.infrastructure.provider.TrainerMapProvider;
import com.solo.ptmatch.trainer.presentation.response.S2ClusterResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerLatLon;
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

    private final TrainerMapProvider trainerMapProvider;

    @Transactional(readOnly = true)
    public List<TrainerLatLon> getMapTrainers(SportType sportType, double minLat, double maxLat, double minLon, double maxLon) {
        // 1. S2 Cell ID 리스트 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        Map<Long, List<TrainerLatLon>> markers = trainerMapProvider.getMarkers(sportType, cellIdLongs);

        // 결과 병합
        return markers.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<S2ClusterResponse> getMapClusters(SportType sportType, double minLat, double maxLat, double minLon, double maxLon, int zoomLevel) {

        int clusterLevel = S2Util.zoomLevelToS2Level(zoomLevel);

        // 1. Level 14 Cell ID 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        Map<Long, Long> counts = trainerMapProvider.getCounts(sportType, cellIdLongs);

        // 6. 클러스터 레벨로 집계
        Map<Long, Long> clusterMap = new HashMap<>();
        for (Long cellId : cellIdLongs) {
            Long count = counts.get(cellId);
            if (count == null) continue;                                                                                                                                                                              

            long parentId = new S2CellId(cellId).parent(clusterLevel).id();
            clusterMap.merge(parentId, count, Long::sum);
        }

        return clusterMap.entrySet().stream()
                .map(e -> S2ClusterResponse.fromCellId(e.getKey(), e.getValue()))
                .toList();
    }
}
