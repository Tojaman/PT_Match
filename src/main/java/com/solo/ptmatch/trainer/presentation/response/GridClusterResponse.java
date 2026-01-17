package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.infrastructure.GridClusterProjection;

// ST_SnapToGrid 기반 지도 클러스터 응답 DTO
public record GridClusterResponse(
        Double gridX,
        Double gridY,
        Double latitude,
        Double longitude,
        Integer trainerCount) {

    public static GridClusterResponse from(GridClusterProjection projection) {
        return new GridClusterResponse(
                projection.getGridX(),
                projection.getGridY(),
                projection.getLatitude(),
                projection.getLongitude(),
                projection.getTrainerCount());
    }
}
