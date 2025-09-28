package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매칭 응답 정보")
public record MatchingResponse(
    @Schema(description = "매칭 ID", example = "200") Long matchingId,
    @Schema(description = "매칭 상태", implementation = MatchingStatus.class, example = "PENDING") MatchingStatus status
) {

    public static MatchingResponse from(Matching matching) {
        return new MatchingResponse(matching.getId(), matching.getStatus());
    }
}
