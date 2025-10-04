package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "받은 매칭 신청 요약")
public record MatchingReceivedSummaryResponse(
    @Schema(description = "매칭 ID", example = "1")
    Long matchingId,

    @Schema(description = "상품 이름", example = "PT 10회 집중관리")
    String productName,

    @Schema(description = "신청 일시", example = "2025-09-16T10:00:00")
    LocalDateTime createdAt
) {

    public static MatchingReceivedSummaryResponse of(Matching matching) {
        return new MatchingReceivedSummaryResponse(
                matching.getId(),
                matching.getProduct().getTitle(),
                matching.getCreatedAt()
        );

    }
}
