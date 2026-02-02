package com.solo.ptmatch.trainer.application;

import com.google.common.geometry.S2CellId;
import com.solo.ptmatch.common.util.S2CellRange;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.S2ClusterResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerLatLon;
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
    private final TrainerMapCacheService trainerMapCacheService;

    @Transactional(readOnly = true)
    public List<TrainerLatLon> getMapTrainers(SportType sportType, double minLat, double maxLat, double minLon, double maxLon) {
        return getMapTrainersMarkers(sportType, minLat, maxLat, minLon, maxLon);
    }

    @Transactional(readOnly = true)
    public List<S2ClusterResponse> getMapClusters(SportType sportType, double minLat, double maxLat, double minLon, double maxLon, int zoomLevel) {

        int clusterLevel = S2Util.zoomLevelToS2Level(zoomLevel);

        // 1. Level 14 Cell ID 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        Map<Long, Long> counts = trainerMapCacheService.getCountsOrLoad(sportType, cellIdLongs,
                missIds -> {
                    List<String> missStrings = missIds.stream().map(String::valueOf).toList();
                    List<S2CellRange> ranges = S2Util.mergeCellIdsToRanges(missStrings);
                    return trainerProfileRepository.countTrainersByCellRanges(sportType, ranges);
                }
        );

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

    // 지도 트레이너 탐색 (S2 Cell ID + Redis 캐시, 종목별)
    // Redis MGET으로 배치 조회 → Cache Miss만 DB 조회 → 캐시 저장
    public List<TrainerLatLon> getMapTrainersMarkers(SportType sportType, double minLat, double maxLat, double minLon,
            double maxLon) {

        // 1. S2 Cell ID 리스트 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        Map<Long, List<TrainerLatLon>> markers = trainerMapCacheService.getMarkersOrLoad(sportType, cellIdLongs,
                missIds -> {
                    List<String> missStrings = missIds.stream().map(String::valueOf).toList();
                    List<S2CellRange> ranges = S2Util.mergeCellIdsToRanges(missStrings);
                    List<TrainerProfile> missedTrainers = trainerProfileRepository.findTrainersByCellRanges(sportType, ranges);
                    // DB에서 조회한 데이터를 S2CellId별로 그룹핑하고 TrainerLatLon으로 변환해서 캐싱 (위경도만 캐싱하기 위함)
                    return missedTrainers.stream()
                            .collect(Collectors.groupingBy(
                                    TrainerProfile::getS2CellId,
                                    Collectors.mapping(TrainerLatLon::from, Collectors.toList())));
                }
        );

        // 결과 병합
        return markers.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}
