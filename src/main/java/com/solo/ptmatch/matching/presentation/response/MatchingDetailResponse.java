package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "매칭 상세 정보")
public record MatchingDetailResponse(
    Long matchingId,
    String message,
    MatchingUserInfo applicantInfo,
    MatchingProductDetailInfo productInfo,
    List<MatchingScheduleInfo> schedules
) {
    public static MatchingDetailResponse of(Matching matching) {
        return new MatchingDetailResponse(
            matching.getId(),
            matching.getMessage(),
            MatchingUserInfo.from(matching.getMatchingUserInfo()),
            MatchingProductDetailInfo.from(matching.getProduct()),
            matching.getSchedules().stream()
                .map(MatchingScheduleInfo::from)
                .toList()
        );
    }
}

