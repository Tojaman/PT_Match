package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "받은 매칭 신청 요약")
public record MatchingReceivedSummaryResponse(
        @Schema(description = "매칭 ID", example = "1") Long matchingId,

        @Schema(description = "회원 이름", example = "홍길동") String userName,

        @Schema(description = "매칭 상태", example = "PENDING") String status,

        @Schema(description = "메시지", example = "PT 받고 싶습니다.") String message,

        @Schema(description = "신청 일시", example = "2025-09-16T10:00:00") LocalDateTime createdAt) {

    public static MatchingReceivedSummaryResponse from(Matching matching) {
        return new MatchingReceivedSummaryResponse(
                matching.getId(),
                matching.getUser().getName(),
                matching.getMatchingStatus().name(),
                matching.getMessage(),
                matching.getCreatedAt());

    }
}
