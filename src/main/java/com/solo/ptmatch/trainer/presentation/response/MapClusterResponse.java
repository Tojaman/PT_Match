package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.infrastructure.MapClusterProjection;

//지도 클러스터 응답 DTO (법정동별 집계)
public record MapClusterResponse(
        String districtCode,
        String districtName,
        Double latitude,
        Double longitude,
        Integer trainerCount) {

    public static MapClusterResponse from (MapClusterProjection mapClusterProjection) {
        return new MapClusterResponse(
                mapClusterProjection.getDistrictCode(),
                mapClusterProjection.getDistrictName(),
                mapClusterProjection.getLatitude(),
                mapClusterProjection.getLongitude(),
                mapClusterProjection.getTrainerCount());
    }
}
