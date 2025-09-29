package com.solo.ptmatch.trainer.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "트레이너 스케줄 응답")
public record TrainerScheduleResponse(
    @Schema(description = "요일", example = "MON")
    String day,
    @Schema(description = "시작 시간", example = "09:00")
    String startTime,
    @Schema(description = "종료 시간", example = "18:00")
    String endTime
) {
}