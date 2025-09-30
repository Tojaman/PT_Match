package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.solo.ptmatch.matching.domain.AvailableSchedule;
import java.time.format.DateTimeFormatter;

@Schema(description = "트레이너 스케줄 응답")
public record TrainerScheduleResponse(
    @Schema(description = "요일", example = "MON")
    String day,
    @Schema(description = "시작 시간", example = "09:00")
    String startTime,
    @Schema(description = "종료 시간", example = "18:00")
    String endTime
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static TrainerScheduleResponse from(AvailableSchedule schedule) {
        return new TrainerScheduleResponse(
                schedule.getStartTime().getDayOfWeek().name(),
                schedule.getStartTime().toLocalTime().format(TIME_FORMATTER),
                schedule.getEndTime().toLocalTime().format(TIME_FORMATTER)
        );
    }
}