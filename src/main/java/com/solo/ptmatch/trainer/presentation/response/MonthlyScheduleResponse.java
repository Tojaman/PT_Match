package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingSchedule;
import java.time.LocalDateTime;

public record MonthlyScheduleResponse(
        Long scheduleId,
        Long matchingId,
        String memberName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String sessionStatus,
        String gym) {

    public static MonthlyScheduleResponse from(MatchingSchedule schedule) {
        return new MonthlyScheduleResponse(
                schedule.getId(),
                schedule.getMatching().getId(),
                schedule.getMatching().getUser().getName(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getSessionStatus().name(),
                schedule.getMatching().getTrainerProfile().getGymAddress());
    }
}
