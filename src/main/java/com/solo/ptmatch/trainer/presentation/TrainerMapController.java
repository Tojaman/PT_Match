package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.TrainerMapService;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.presentation.response.S2ClusterResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerLatLon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@Tag(name = "TrainerMap", description = "트레이너 지도 API (마커, 클러스터링)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainers/maps")
public class TrainerMapController {

        private final TrainerMapService trainerMapService;

        @Operation(summary = "지도 트레이너 마커 조회 (S2 Cell ID + Redis)", description = "Redis MGET 배치 조회 기반 캐시 적용 트레이너 목록 조회")
        @GetMapping("/markers-s2-cached")
        public ResponseEntity<ApiResponse<List<TrainerLatLon>>> getMapTrainers(
                @RequestParam SportType sportType,
                @RequestParam double minLat,
                @RequestParam double maxLat,
                @RequestParam double minLon,
                @RequestParam double maxLon) {
            List<TrainerLatLon> trainers = trainerMapService.getMapTrainersMarkers(sportType, minLat, maxLat, minLon, maxLon);
            return ResponseEntity.ok(ApiResponse.success(trainers));
        }

        @Operation(summary = "S2 클러스터링 v2 (count 캐시)", description = "count만 조회하여 메모리 최적화 + BETWEEN 쿼리 최적화")
        @GetMapping("/clusters-s2-cached-v2")
        public ResponseEntity<ApiResponse<List<S2ClusterResponse>>> getMapClusters(
                @RequestParam SportType sportType,
                @RequestParam double minLat,
                @RequestParam double maxLat,
                @RequestParam double minLon,
                @RequestParam double maxLon,
                @RequestParam int zoomLevel) {
                List<S2ClusterResponse> clusters = trainerMapService.getMapClusters(sportType, minLat, maxLat, minLon, maxLon, zoomLevel);
            return ResponseEntity.ok(ApiResponse.success(clusters));
        }
}
