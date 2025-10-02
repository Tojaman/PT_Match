package com.solo.ptmatch.trainer.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "트레이너 스케줄 수정 요청")
public record TrainerScheduleUpdateRequest(
        @Schema(description = "수정할 스케줄 목록")
        @NotEmpty
        List<TrainerScheduleUpdateRequestItem> schedules
) {
}
