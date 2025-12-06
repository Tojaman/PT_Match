package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "상품 핵심 정보")
public record MatchingProductInfo(
        @Schema(description = "상품 ID", example = "1") Long productId,
        @Schema(description = "상품 제목", example = "초급자용 1:1 PT") String productTitle,
        @Schema(description = "회당 가격", example = "50000") BigDecimal pricePerSession,
        @Schema(description = "총 횟수", example = "10") Integer sessionCount) {
    public static MatchingProductInfo from(Product product) {
        return new MatchingProductInfo(
                product.getId(),
                product.getTitle(),
                product.getPricePerSession(),
                product.getSessionCount());
    }
}
