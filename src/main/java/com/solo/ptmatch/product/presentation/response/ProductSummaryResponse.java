package com.solo.ptmatch.product.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "PT 상품 요약")
public record ProductSummaryResponse(
    @Schema(description = "상품 ID", example = "2")
    Long productId,
    @Schema(description = "상품명")
    String name,
    @Schema(description = "총 가격 또는 대표 가격")
    BigDecimal totalPrice,
    @Schema(description = "트레이너 이름")
    String trainerName,
    @Schema(description = "카테고리")
    String category,
    @Schema(description = "썸네일 URL")
    String thumbnailUrl,
    @Schema(description = "좋아요 수")
    Long likesCount
) {
}
