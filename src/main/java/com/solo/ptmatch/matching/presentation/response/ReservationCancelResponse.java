package com.solo.ptmatch.matching.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 취소 결과")
public record ReservationCancelResponse(
    @Schema(description = "처리 메시지", example = "예약이 취소되었습니다.")
    String message
) {
}
