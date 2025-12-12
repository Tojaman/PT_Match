package com.solo.ptmatch.trainer.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Schema(description = "트레이너 스케줄 등록")
public record TrainerScheduleCreateRequest(
        @Schema(description = "스케줄 리스트", example = "")
        @NotEmpty
        Set<TrainerScheduleRequest> schedules) {
}
