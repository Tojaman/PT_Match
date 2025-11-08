package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TrainerScheduleRequest(
        Long scheduleId,

        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime
) {
}