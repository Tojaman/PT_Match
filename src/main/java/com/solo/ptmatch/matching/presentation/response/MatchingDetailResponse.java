package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.matching.domain.MatchingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "매칭 상세 정보")
public record MatchingDetailResponse(
    @Schema(description = "매칭 ID", example = "200") Long matchingId,
    @Schema(description = "회원 ID", example = "1") Long memberId,
    @Schema(description = "트레이너 프로필 ID", example = "10") Long trainerProfileId,
    @Schema(description = "상품 ID", example = "100") Long productId,
    @Schema(description = "매칭 상태", implementation = MatchingStatus.class, example = "ACCEPTED") MatchingStatus status,
    @Schema(description = "매칭 메시지", example = "주 2회 레슨을 희망합니다.") String message,
    @Schema(description = "생성 일시") LocalDateTime createdAt,
    @Schema(description = "수정 일시") LocalDateTime updatedAt
) {
}
