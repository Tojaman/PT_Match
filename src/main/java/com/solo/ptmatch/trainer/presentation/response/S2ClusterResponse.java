package com.solo.ptmatch.trainer.presentation.response;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;

public record S2ClusterResponse(
        Long s2CellId,
        Double latitude,
        Double longitude,
        Long trainerCount) {

    public static S2ClusterResponse fromCellId(Long cellId, Long count) {
        S2CellId s2CellId = new S2CellId(cellId);
        S2LatLng center = s2CellId.toLatLng();
        return new S2ClusterResponse(
                cellId,
                center.latDegrees(),
                center.lngDegrees(),
                count);
    }
}
