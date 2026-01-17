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

    // for 마커 조회
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

    // for 클러스터 조회
    public static List<S2CellRange> getCoveringRanges(
            double minLat, double maxLat, double minLon, double maxLon, int level) {

        S2LatLngRect rect = S2LatLngRect.fromPointPair(
                S2LatLng.fromDegrees(minLat, minLon),
                S2LatLng.fromDegrees(maxLat, maxLon));

        // 내부 최적화로 level의 상위 level 반환 가능
        S2RegionCoverer coverer = S2RegionCoverer.builder()
                .setMinLevel(level)
                .setMaxLevel(level)
                .setMaxCells(200)
                .build();

        S2CellUnion covering = coverer.getCovering(rect);

        return cellsToIndividualRanges(covering);
    }

    // for 클러스터 조회 (BETWEEN 최소화 버전)
    public static List<S2CellRange> getCoveringRangesMerge(
            double minLat, double maxLat, double minLon, double maxLon) {

        S2LatLngRect rect = S2LatLngRect.fromPointPair(
                S2LatLng.fromDegrees(minLat, minLon),
                S2LatLng.fromDegrees(maxLat, maxLon));

        // S2RegionCoverer 설정
        // level을 고정해도 자식 레벨들이 상위 level을 포함하면 상위 level 반환
        // ex. Lv.14 셀들 4개가 Lv.13 셀에 포함되면 Lv.13 셀 반환
        S2RegionCoverer coverer = S2RegionCoverer.builder()
                .setMinLevel(STORAGE_LEVEL) // 14레벨 셀 고정 (DB에 14레벨로 저장 (캐시 Hit))
                .setMaxLevel(STORAGE_LEVEL)
                .setMaxCells(200)
                .build();

        S2CellUnion covering = coverer.getCovering(rect);

        // 연속된 셀을 병합하여 Range 수 최소화 (내부에서 Level 14 확장)
        return mergeConsecutiveRanges(covering);
    }

    // 최소 레벨로 제한된 셀들을 범위로 반환
    private static List<S2CellRange> cellsToIndividualRanges(S2CellUnion covering) {
        List<S2CellRange> ranges = new ArrayList<>();

        for (int i = 0; i < covering.size(); i++) {
            S2CellId cell = covering.cellId(i);
            ranges.add(new S2CellRange(cell.rangeMin().id(), cell.rangeMax().id()));
        }

        return ranges;
    }

    /**
     * 연속된 S2 Cell ID를 범위로 병합
     * Hilbert curve 특성 활용: 인접 셀 ID가 대체로 연속
     * 이를 통해 OR 조건 수를 최소화하여 쿼리 성능 향상
     */
    private static List<S2CellRange> mergeConsecutiveRanges(S2CellUnion covering) {
        List<S2CellRange> ranges = new ArrayList<>();

        if (covering.size() == 0) {
            return ranges;
        }

        // S2RegionCoverer 내부 최적화로 부모 Level이 반환될 수 있으므로 Level 14로 확장
        List<S2CellId> cells = new ArrayList<>();
        for (int i = 0; i < covering.size(); i++) {
            S2CellId cell = covering.cellId(i);
            if (cell.level() < STORAGE_LEVEL) {
                expandToLevel(cell, STORAGE_LEVEL, cells); // 14레벨로 확장
            } else {
                cells.add(cell);
            }
        }

        // 셀 ID 정렬
        cells.sort((a, b) -> Long.compare(a.id(), b.id()));

        long rangeStart = cells.get(0).id();
        long rangeEnd = cells.get(0).id();

        // 연속된 Cell은 합쳐서 BETWEEN 쿼리 최소화
        for (int i = 1; i < cells.size(); i++) {
            // Level 14 기준 연속성 판단: prev.next() == current
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
}
