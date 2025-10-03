package com.solo.ptmatch.trainer.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "트레이너 스케줄 삭제 요청")
public record TrainerScheduleDeleteRequest(
        @Schema(description = "삭제할 스케줄 ID 목록", example = "[101, 102]")
        @NotEmpty
        List<Long> scheduleIds
) {
}
