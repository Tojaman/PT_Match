package com.solo.ptmatch.common.util;

// 위경도 기반 Bounding Box 계산 유틸리티
public record CoordinateBounds(
        double minLat,
        double maxLat,
        double minLng,
        double maxLng) {
    // 한국 위도(약 37도) 기준 상수
    private static final double KM_PER_LAT_DEGREE = 111.0;
    private static final double KM_PER_LNG_DEGREE = 88.0;

    // 중심 좌표와 반경으로부터 Bounding Box 생성
    public static CoordinateBounds of(double centerLat, double centerLng, double radiusKm) {
        double latDiff = radiusKm / KM_PER_LAT_DEGREE;
        double lngDiff = radiusKm / KM_PER_LNG_DEGREE;

        return new CoordinateBounds(
                centerLat - latDiff,
                centerLat + latDiff,
                centerLng - lngDiff,
                centerLng + lngDiff);
    }
}
