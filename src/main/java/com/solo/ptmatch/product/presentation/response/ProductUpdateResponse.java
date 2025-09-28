package com.solo.ptmatch.product.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "PT 상품 수정 응답")
public record ProductUpdateResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,
    @Schema(description = "상품명")
    String name,
    @Schema(description = "회당 가격")
    BigDecimal pricePerSession,
    @Schema(description = "세션 수")
    int sessionCount
) {
}
