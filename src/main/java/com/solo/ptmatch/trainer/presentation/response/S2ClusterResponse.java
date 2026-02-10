package com.solo.ptmatch.trainer.presentation.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record S2ClusterResponse(
        @JsonSerialize(using = ToStringSerializer.class) // 내부적으로 Long 사용하고 응답만 String으로
        Long s2CellId,
        Double latitude,
        Double longitude,
        Long trainerCount) {

    public static S2ClusterResponse from(Long cellId, double latitude, double longitude, long count) {
        return new S2ClusterResponse(cellId, latitude, longitude, count);
    }
}
