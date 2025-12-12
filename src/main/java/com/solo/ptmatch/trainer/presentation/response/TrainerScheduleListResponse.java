package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TrainerScheduleListResponse(

        Long scheduleId,

        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime,

        ReservationStatus reservationStatus) {

    public static TrainerScheduleListResponse of(Long scheduleId, LocalDateTime startTime, LocalDateTime endTime, ReservationStatus reservationStatus) {
        return new TrainerScheduleListResponse(
                scheduleId,
                startTime,
                endTime,
                reservationStatus);
    }

    public static TrainerScheduleListResponse from(AvailableSchedule availableSchedule) {
        return new TrainerScheduleListResponse(
                availableSchedule.getId(),
                availableSchedule.getStartTime(),
                availableSchedule.getEndTime(),
                availableSchedule.getReservationStatus());
    }
}
