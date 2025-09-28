package com.solo.ptmatch.matching.presentation.request;

import com.solo.ptmatch.matching.domain.MatchingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "매칭 응답 요청")
public record MatchingRespondRequest(
    @Schema(description = "응답 상태", example = "ACCEPTED")
    @NotNull
    MatchingStatus status
) {
}
