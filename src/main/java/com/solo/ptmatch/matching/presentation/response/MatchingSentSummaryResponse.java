package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보낸 매칭 신청 요약")
public record MatchingSentSummaryResponse(
        Long matchingId,
        MatchingStatus status,
        MatchingTrainerInfo matchingTrainerInfo,
        @Schema(description = "신청 메시지", example = "PT 받고 싶습니다.") String message,
        @Schema(description = "신청 일시", example = "2023-11-20T10:00:00") java.time.LocalDateTime requestDate) {
    public static MatchingSentSummaryResponse from(Matching matching) {
        return new MatchingSentSummaryResponse(
                matching.getId(),
                matching.getMatchingStatus(),
                MatchingTrainerInfo.from(matching.getTrainerProfile()),
                matching.getMessage(),
                matching.getCreatedAt());
    }
}
