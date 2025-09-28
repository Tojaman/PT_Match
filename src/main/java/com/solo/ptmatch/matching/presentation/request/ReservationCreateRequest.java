package com.solo.ptmatch.matching.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "예약 생성 요청")
public record ReservationCreateRequest(
    @Schema(description = "매칭 ID", example = "1")
    @NotNull
    Long matchingId,

    @Schema(description = "예약할 스케줄 ID", example = "101")
    @NotNull
    Long scheduleId
) {
}
