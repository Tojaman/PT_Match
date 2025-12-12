package com.solo.ptmatch.trainer.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "트레이너 스케줄 생성/수정")
public record TrainerScheduleRequest(
    @Schema(description = "스케줄 ID (수정 시 필요)", example = "1")
    Long scheduleId,

    @Schema(description = "시작 시간", example = "2025-01-01T09:00:00")
    @NotNull
    LocalDateTime startTime,

    @Schema(description = "종료 시간", example = "2025-01-01T10:00:00")
    @NotNull
    LocalDateTime endTime) {
}