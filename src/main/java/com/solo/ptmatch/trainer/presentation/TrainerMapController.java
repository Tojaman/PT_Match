package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.TrainerMapService;
import com.solo.ptmatch.trainer.presentation.response.GridClusterResponse;
import com.solo.ptmatch.trainer.presentation.response.S2ClusterResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
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

    // ==================== 지도 마커 ====================
    @Operation(summary = "지도 트레이너 탐색 (Bounding Box)", description = "순수 Bounding Box 기반 영역 내 트레이너 목록 조회")
    @GetMapping("/markers-bbox")
    public ResponseEntity<ApiResponse<List<TrainerSummaryResponse>>> getMapTrainersBbox(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {
        List<TrainerSummaryResponse> trainers = trainerMapService.getMapTrainersBbox(minLat, maxLat, minLon, maxLon);
        return ResponseEntity.ok(ApiResponse.success(trainers));
    }

    @Operation(summary = "지도 트레이너 탐색 (S2 Cell ID)", description = "S2 Cell ID IN 쿼리 기반 영역 내 트레이너 목록 조회")
    @GetMapping("/markers-s2")
    public ResponseEntity<ApiResponse<List<TrainerSummaryResponse>>> getMapTrainersS2(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {
        List<TrainerSummaryResponse> trainers = trainerMapService.getMapTrainersS2(minLat, maxLat, minLon, maxLon);
        return ResponseEntity.ok(ApiResponse.success(trainers));
    }

    // ==================== 클러스터링 ====================
    @Operation(summary = "SnapToGrid + BETWEEN 기반 클러스터 조회", description = "BETWEEN 조건 + PostGIS ST_SnapToGrid 기반 트레이너 클러스터링")
    @GetMapping("/clusters-snaptogrid-between")
    public ResponseEntity<ApiResponse<List<GridClusterResponse>>> getMapClustersSnapToGridBetween(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon,
            @RequestParam int zoomLevel) {
        List<GridClusterResponse> clusters = trainerMapService.getMapClustersSnapToGridBetween(minLat, maxLat, minLon,
                maxLon, zoomLevel);
        return ResponseEntity.ok(ApiResponse.success(clusters));
    }

    @Operation(summary = "S2 Geometry 기반 클러스터 조회", description = "S2 BETWEEN 범위 쿼리 기반 트레이너 클러스터링")
    @GetMapping("/clusters-s2-java")
    public ResponseEntity<ApiResponse<List<S2ClusterResponse>>> getMapClustersS2Java(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon,
            @RequestParam int zoomLevel) {
        List<S2ClusterResponse> clusters = trainerMapService.getMapClustersS2Java(minLat, maxLat, minLon, maxLon,
                zoomLevel);
        return ResponseEntity.ok(ApiResponse.success(clusters));
    }

    @Operation(summary = "S2 Geometry 기반 클러스터 조회 (Merge 최적화)", description = "S2 BETWEEN 범위 쿼리 + 인접 셀 병합 기반 트레이너 클러스터링")
    @GetMapping("/clusters-s2-java-merge")
    public ResponseEntity<ApiResponse<List<S2ClusterResponse>>> getMapClustersS2JavaMerge(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon,
            @RequestParam int zoomLevel) {
        List<S2ClusterResponse> clusters = trainerMapService.getMapClustersS2JavaMerge(minLat, maxLat, minLon, maxLon,
                zoomLevel);
        return ResponseEntity.ok(ApiResponse.success(clusters));
    }

    // ==================== Redis 캐시 적용 API ====================

    @Operation(summary = "지도 트레이너 마커 조회 (S2 Cell ID + Redis)", description = "Redis MGET 배치 조회 기반 캐시 적용 트레이너 목록 조회")
    @GetMapping("/markers-s2-cached")
    public ResponseEntity<ApiResponse<List<TrainerSummaryResponse>>> getMapTrainersS2Cached(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {
        List<TrainerSummaryResponse> trainers = trainerMapService.getMapTrainersMarkers(minLat, maxLat, minLon,
                maxLon);
        return ResponseEntity.ok(ApiResponse.success(trainers));
    }

    @Operation(summary = "S2 Geometry 기반 클러스터 조회 (Redis)", description = "Redis 캐시 적용 + Java 집계 기반 트레이너 클러스터링")
    @GetMapping("/clusters-s2-cached")
    public ResponseEntity<ApiResponse<List<S2ClusterResponse>>> getMapClustersS2Cached(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon,
            @RequestParam int zoomLevel) {
        List<S2ClusterResponse> clusters = trainerMapService.getMapClustersS2Cached(minLat, maxLat, minLon, maxLon,
                zoomLevel);
        return ResponseEntity.ok(ApiResponse.success(clusters));
    }
}
