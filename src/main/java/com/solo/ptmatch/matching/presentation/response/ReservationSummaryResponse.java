package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "예약 요약")
public record ReservationSummaryResponse(
    @Schema(description = "예약 ID", example = "201")
    Long reservationId,

    @Schema(description = "상대방 이름", example = "박전문")
    String partnerName,

    @Schema(description = "예약 시간", example = "2025-09-22T10:00:00")
    LocalDateTime reservationTime,

    @Schema(description = "예약 상태", example = "SCHEDULED")
    ReservationStatus status
) {
}
