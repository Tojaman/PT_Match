package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TrainerScheduleListResponse(

        Long scheduleId,

        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime
) {
        public static TrainerScheduleListResponse of(Long scheduleId, LocalDateTime startTime, LocalDateTime endTime) {
                return new TrainerScheduleListResponse(
                        scheduleId,
                        startTime,
                        endTime
                );
        }

        public static TrainerScheduleListResponse from(AvailableSchedule availableSchedule) {
                return new TrainerScheduleListResponse(
                        availableSchedule.getId(),
                        availableSchedule.getStartTime(),
                        availableSchedule.getEndTime()
                );
        }
}
