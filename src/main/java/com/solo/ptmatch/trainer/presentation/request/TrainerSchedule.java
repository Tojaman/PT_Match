package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TrainerSchedule(
        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime
) {
        public static TrainerSchedule of(LocalDateTime startTime, LocalDateTime endTime) {
                return new TrainerSchedule(
                        startTime,
                        endTime
                );
        }
}
