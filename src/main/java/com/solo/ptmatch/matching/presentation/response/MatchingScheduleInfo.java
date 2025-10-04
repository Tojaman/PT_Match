package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingSchedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "매칭된 스케줄 정보")
public record MatchingScheduleInfo(
    LocalDateTime startTime,
    LocalDateTime endTime
) {
    public static MatchingScheduleInfo from(MatchingSchedule schedule) {
        return new MatchingScheduleInfo(schedule.getStartTime(), schedule.getEndTime());
    }
}
