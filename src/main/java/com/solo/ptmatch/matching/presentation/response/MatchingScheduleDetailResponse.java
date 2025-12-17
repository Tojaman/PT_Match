package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.domain.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "매칭 스케줄 상세 정보")
public record MatchingScheduleDetailResponse(
        @Schema(description = "스케줄 ID", example = "1") Long scheduleId,

        @Schema(description = "매칭 ID", example = "10") Long matchingId,

        @Schema(description = "트레이너 이름", example = "김트레이너") String trainerName,

        @Schema(description = "활동 지점", example = "강남점") String gymName,

        @Schema(description = "시작 시간", example = "2025-12-20T19:00:00") LocalDateTime startTime,

        @Schema(description = "종료 시간", example = "2025-12-20T20:00:00") LocalDateTime endTime,

        @Schema(description = "진행 상태", example = "SCHEDULED") SessionStatus status) {
    public static MatchingScheduleDetailResponse from(MatchingSchedule schedule) {
        return new MatchingScheduleDetailResponse(
                schedule.getId(),
                schedule.getMatching().getId(),
                schedule.getMatching().getTrainerProfile().getUser().getName(),
                schedule.getMatching().getTrainerProfile().getGymAddress(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSessionStatus());
    }
}
