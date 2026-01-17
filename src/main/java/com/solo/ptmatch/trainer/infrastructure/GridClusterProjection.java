package com.solo.ptmatch.trainer.infrastructure;

// ST_SnapToGrid 기반 클러스터링 네이티브 쿼리 결과 매핑 인터페이스
public interface GridClusterProjection {
    Double getGridX(); // 그리드 셀 중심 X (경도)

    Double getGridY(); // 그리드 셀 중심 Y (위도)

    Double getLatitude(); // 클러스터 평균 위도

    Double getLongitude(); // 클러스터 평균 경도

    Integer getTrainerCount();
}
