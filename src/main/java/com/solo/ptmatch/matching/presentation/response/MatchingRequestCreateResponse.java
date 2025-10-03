package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "매칭 신청 결과")
public record MatchingRequestCreateResponse(
    @Schema(description = "매칭 ID", example = "1")
    Long matchingId,

    @Schema(description = "매칭 상태", example = "PENDING")
    MatchingStatus status,

    LocalDateTime startTime,
    LocalDateTime endTime,

    MatchingUserInfo matchingUserInfo
) {
    public static MatchingRequestCreateResponse of(Long matchingId,
                                            MatchingStatus status,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime,
                                            MatchingUserInfo matchingUserInfo) {
        return new MatchingRequestCreateResponse(matchingId, status, startTime, endTime, matchingUserInfo);
    }
}
