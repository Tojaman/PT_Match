package com.solo.ptmatch.common.util;

import com.google.common.geometry.*;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class S2Util {

    // DB에 저장된 레벨
    public static final int STORAGE_LEVEL = 14;

    public static long calculateS2CellId(double latitude, double longitude) {
        S2LatLng latLng = S2LatLng.fromDegrees(latitude, longitude);
        return S2CellId.fromLatLng(latLng).parent(STORAGE_LEVEL).id();
    }

    public static int zoomLevelToS2Level(int zoomLevel) {
        // 줌 레벨에 맞는 S2 Level 반환
        return switch (zoomLevel) {
            case 4, 5 -> 14;
            case 6 -> 13;
            case 7 -> 12;
            case 8 -> 11;
            case 9, 10 -> 10;
            default -> 9;
        };
    }

    // === 마커 ===
    public static List<S2CellId> getCoveringCellIds(double minLat, double maxLat, double minLon, double maxLon, int level) {

        S2LatLngRect rect = S2LatLngRect.fromPointPair(
                S2LatLng.fromDegrees(minLat, minLon),
                S2LatLng.fromDegrees(maxLat, maxLon));

        S2RegionCoverer coverer = S2RegionCoverer.builder()
                .setMinLevel(level)
                .setMaxLevel(level)
                .build();

        S2CellUnion covering = coverer.getCovering(rect);

        // S2RegionCoverer는 최적화를 위해 상위 레벨 셀을 반환할 수 있음
        // (예: Level 13 셀이 Level 14 자식 4개를 모두 포함하는 경우)
        // 따라서 모든 셀을 타겟 레벨로 확장
        List<S2CellId> result = new ArrayList<>();
        for (int i = 0; i < covering.size(); i++) {
            S2CellId cell = covering.cellId(i);
            if (cell.level() == level) {
                result.add(cell);
            } else if (cell.level() < level) {
                // 상위 레벨 셀을 타겟 레벨 자식들로 확장
                expandToLevel(cell, level, result);
            }
        }

        return result;
    }
    // 상위 레벨 Cell -> 14레벨 Cell
    private static void expandToLevel(S2CellId cell, int targetLevel, List<S2CellId> result) {
        if (cell.level() == targetLevel) {
            result.add(cell);
            return;
        }

        // 4개의 자식 셀로 재귀 확장
        for (int i = 0; i < 4; i++) {
            expandToLevel(cell.child(i), targetLevel, result);
        }
    }

    // === 클러스터 ===
    // Cache Miss된 Cell ID 목록을 연속된 범위로 병합 (DB 조회 최적화)
    public static List<S2CellRange> mergeCellIdsToRanges(List<Long> cellIds) {
        if (cellIds == null || cellIds.isEmpty()) {
            return List.of();
        }

        // Cell ID를 S2CellId로 변환 후 정렬
        List<S2CellId> cells = cellIds.stream()
                .map(S2CellId::new)
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .toList();

        List<S2CellRange> ranges = new ArrayList<>();
        long rangeStart = cells.get(0).id();
        long rangeEnd = cells.get(0).id();

        // 연속된 Cell은 합쳐서 BETWEEN 쿼리 최소화
        for (int i = 1; i < cells.size(); i++) {
            if (cells.get(i - 1).next().equals(cells.get(i))) {
                // 연속 → 범위 확장
                rangeEnd = cells.get(i).id();
            } else {
                // 불연속 → 이전 범위 저장하고 새 범위 시작
                ranges.add(new S2CellRange(rangeStart, rangeEnd));
                rangeStart = cells.get(i).id();
                rangeEnd = cells.get(i).id();
            }
        }
        // 마지막 범위 저장
        ranges.add(new S2CellRange(rangeStart, rangeEnd));

        return ranges;
    }
}
