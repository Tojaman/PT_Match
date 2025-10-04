package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MatchingScheduleSummary(
        @Schema(description = "매칭 세션 ID", example = "10")
        Long matchingScheduleId,

        @Schema(description = "선택된 예약 가능 스케줄 ID", example = "3")
        Long availableScheduleId,

        @Schema(description = "세션 시작 시각", example = "2024-02-10T09:00:00")
        LocalDateTime startTime,

        @Schema(description = "세션 종료 시각", example = "2024-02-10T10:00:00")
        LocalDateTime endTime,

        @Schema(description = "세션 상태", example = "SCHEDULED")
        SessionStatus sessionStatus
) {
    public static MatchingScheduleSummary from(
            Long matchingScheduleId,
            Long availableScheduleId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            SessionStatus sessionStatus
    ) {
        return new MatchingScheduleSummary(
                matchingScheduleId,
                availableScheduleId,
                startTime,
                endTime,
                sessionStatus
        );
    }
}