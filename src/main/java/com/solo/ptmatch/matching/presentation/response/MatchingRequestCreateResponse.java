package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.presentation.response.MatchingScheduleSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "매칭 신청 결과")
public record MatchingRequestCreateResponse(
    @Schema(description = "매칭 ID", example = "1")
    Long matchingId,

    @Schema(description = "매칭 상태", example = "PENDING")
    MatchingStatus status,

    @Schema(description = "신청된 세션 정보 목록")
    List<MatchingScheduleSummary> schedules,

    MatchingUserInfo matchingUserInfo
) {
    public static MatchingRequestCreateResponse from(
            Matching matching
    ) {
        // 단순한 매핑 로직이므로 DTO 내부에 있어도 괜찮다 판단
        List<MatchingScheduleSummary> scheduleSummaries = matching.getSchedules().stream()
                .map(MatchingScheduleSummary::from)
                .toList();
        return new MatchingRequestCreateResponse(
                matching.getId(),
                matching.getMatchingStatus(),
                scheduleSummaries,
                matching.getMatchingUserInfo()
        );
    }

    public static MatchingRequestCreateResponse of(
            Long matchingId,
            MatchingStatus status,
            List<MatchingScheduleSummary> schedules,
            MatchingUserInfo matchingUserInfo
    ) {
        return new MatchingRequestCreateResponse(matchingId, status, schedules, matchingUserInfo);
    }
}
