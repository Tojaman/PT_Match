package com.solo.ptmatch.matching.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "매칭 신청 요청")
public record MatchingRequestCreateRequest(

    @Schema(description = "신청할 트레이너 프로필 ID", example = "1")
    @NotNull
    Long trainerProfileId,

    @Schema(description = "선택한 예약 가능 스케줄 ID 목록", example = "[1,2,3]")
    @NotEmpty
    List<@NotNull Long> availableScheduleIds,

    @Schema(description = "신청 메시지", example = "주 2회 PT 받고 싶습니다. 시간 조율 원합니다.")
    @NotBlank
    @Size(max = 500)
    String message,

    @Valid
    @NotNull
    UserInfo userInfo
) {
}
