package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "예약 상세")
public record ReservationDetailResponse(
    @Schema(description = "예약 ID", example = "201")
    Long reservationId,

    @Schema(description = "매칭 ID", example = "1")
    Long matchingId,

    @Schema(description = "트레이너 이름", example = "박전문")
    String trainerName,

    @Schema(description = "회원 이름", example = "김헬스")
    String userName,

    @Schema(description = "예약 시간", example = "2025-09-22T10:00:00")
    LocalDateTime reservationTime,

    @Schema(description = "예약 상태", example = "PENDING")
    ReservationStatus status
) {
}
