package com.solo.ptmatch.trainer.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "수정할 트레이너 스케줄")
public record TrainerScheduleUpdateRequestItem(
        @Schema(description = "스케줄 식별자", example = "101")
        @NotNull
        Long scheduleId,

        @Schema(description = "수정된 시작 시간", example = "2025-09-22T10:00:00")
        @NotNull
        LocalDateTime startTime,

        @Schema(description = "수정된 종료 시간", example = "2025-09-22T11:00:00")
        @NotNull
        LocalDateTime endTime
) {
}
