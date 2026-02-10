package com.solo.ptmatch.trainer.application;

import com.google.common.geometry.S2CellId;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.infrastructure.CellStat;
import com.solo.ptmatch.trainer.infrastructure.provider.TrainerMapProvider;
import com.solo.ptmatch.trainer.presentation.response.S2ClusterResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerMarker;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryCursorResponse;
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

    private static final int DEFAULT_SUMMARY_PAGE_SIZE = 20;
    private static final int MAX_SUMMARY_PAGE_SIZE = 100;

    private final TrainerMapProvider trainerMapProvider;

    @Transactional(readOnly = true)
    public List<TrainerMarker> getMapTrainers(SportType sportType, double minLat, double maxLat, double minLon, double maxLon) {
        // 1. S2 Cell ID 리스트 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        Map<Long, List<TrainerMarker>> markers = trainerMapProvider.getMarkers(sportType, cellIdLongs);

        // 결과 병합
        return markers.values().stream()
                .flatMap(List::stream)
                .filter(t -> t.latitude() >= minLat && t.latitude() <= maxLat
                        && t.longitude() >= minLon && t.longitude() <= maxLon)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainerSummaryCursorResponse getTrainerSummariesByIds(List<Long> trainerIds, int cursor, int size) {
        int end = Math.min(cursor + size, trainerIds.size());
        List<Long> pageTrainerIds = trainerIds.subList(cursor, end);
        List<TrainerSummaryResponse> summaries = trainerMapProvider.getTrainerSummaries(pageTrainerIds);

        boolean hasNext = end < trainerIds.size();
        Integer nextCursor = hasNext ? end : null;

        return new TrainerSummaryCursorResponse(summaries, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public List<S2ClusterResponse> getMapClusters(SportType sportType, double minLat, double maxLat, double minLon, double maxLon, int zoomLevel) {

        int clusterLevel = S2Util.zoomLevelToS2Level(zoomLevel);

        // 1. Level 14 Cell ID 생성
        List<S2CellId> cellIds = S2Util.getCoveringCellIds(minLat, maxLat, minLon, maxLon, S2Util.STORAGE_LEVEL);
        List<Long> cellIdLongs = cellIds.stream().map(S2CellId::id).toList();

        Map<Long, CellStat> counts = trainerMapProvider.getCounts(sportType, cellIdLongs);

        Map<Long, CellStat> clusterMap = aggregateToClusterLevel(cellIdLongs, counts, clusterLevel);

        // return toClusterResponses(clusterMap);
        List<S2ClusterResponse> clusterResponses = toClusterResponses(clusterMap);
        return clusterResponses;
    }

    // 클러스터 레벨로 집계
    private Map<Long, CellStat> aggregateToClusterLevel(List<Long> cellIdLongs, Map<Long, CellStat> counts, int clusterLevel) {
        Map<Long, CellStat> clusterMap = new HashMap<>();
        for (Long cellId : cellIdLongs) {
            CellStat stat = counts.get(cellId);
            if (stat.count() == 0) continue;

            long parentId = new S2CellId(cellId).parent(clusterLevel).id();
            clusterMap.merge(parentId, stat, CellStat::add);
        }
        return clusterMap;
    }

    // 평균치 계산해서 응답
    private List<S2ClusterResponse> toClusterResponses(Map<Long, CellStat> clusterMap) {
        return clusterMap.entrySet().stream()
                .map(e -> {
                    CellStat stat = e.getValue();
                    return S2ClusterResponse.from(
                            e.getKey(),
                            stat.centroidLatitude(),
                            stat.centroidLongitude(),
                            stat.count()
                    );
                })
                .toList();
    }
}
