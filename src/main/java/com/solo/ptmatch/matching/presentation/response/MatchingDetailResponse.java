package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "매칭 상세 정보")
public record MatchingDetailResponse(
    Long matchingId,
    String message,
    MatchingUserInfo applicantInfo,
    List<MatchingScheduleInfo> schedules
) {
    public static MatchingDetailResponse from(Matching matching) {
        return new MatchingDetailResponse(
            matching.getId(),
            matching.getMessage(),
            MatchingUserInfo.from(matching.getMatchingUserInfo()),
            matching.getSchedules().stream()
                .map(MatchingScheduleInfo::from)
                .toList()
        );
    }
}

