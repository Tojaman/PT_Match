package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "보낸 매칭 신청 요약")
public record MatchingSentSummaryResponse(
    Long matchingId,
    MatchingStatus status,
    MatchingTrainerInfo matchingTrainerInfo,
    MatchingProductInfo matchingProductInfo
) {
    public static MatchingSentSummaryResponse of(Matching matching) {
        return new MatchingSentSummaryResponse(
            matching.getId(),
            matching.getMatchingStatus(),
            MatchingTrainerInfo.from(matching.getTrainerProfile()),
            MatchingProductInfo.from(matching.getProduct())
        );
    }
}
