package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.presentation.request.TrainerSchedule;
import io.swagger.v3.oas.annotations.media.Schema;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "트레이너 스케줄 응답")
public record TrainerScheduleListResponse(
        @Schema(description = "스케줄 리스트", example = "09:00")
        List<TrainerSchedule> shedules
) {

    public static TrainerScheduleListResponse from(List<TrainerSchedule> schedules) {
        return new TrainerScheduleListResponse(
                schedules
        );
    }
}