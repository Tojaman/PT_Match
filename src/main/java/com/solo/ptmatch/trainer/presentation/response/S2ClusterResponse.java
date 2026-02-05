package com.solo.ptmatch.trainer.presentation.response;

public record S2ClusterResponse(
        Long s2CellId,
        Double latitude,
        Double longitude,
        Long trainerCount) {

    public static S2ClusterResponse from(Long cellId, double latitude, double longitude, long count) {
        return new S2ClusterResponse(cellId, latitude, longitude, count);
    }
}
