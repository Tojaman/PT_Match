package com.solo.ptmatch.trainer.infrastructure;

/**
 * 네이티브 쿼리 결과를 매핑하기 위한 프로젝션 인터페이스
 */
public interface MapClusterProjection {
    String getDistrictCode();

    String getDistrictName();

    Double getLatitude();

    Double getLongitude();

    Integer getTrainerCount();
}
