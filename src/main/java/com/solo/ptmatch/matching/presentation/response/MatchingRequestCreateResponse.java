package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매칭 신청 결과")
public record MatchingRequestCreateResponse(
    @Schema(description = "매칭 ID", example = "1")
    Long matchingId,

    @Schema(description = "매칭 상태", example = "PENDING")
    MatchingStatus status
) {
}
