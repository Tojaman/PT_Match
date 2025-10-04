package com.solo.ptmatch.matching.presentation.response;

import com.solo.ptmatch.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "상품 상세 정보")
public record MatchingProductDetailInfo(
    Long productId,
    String productName,
    BigDecimal pricePerSession,
    int sessionCount
) {
    public static MatchingProductDetailInfo from(Product product) {
        return new MatchingProductDetailInfo(
            product.getId(),
            product.getTitle(),
            product.getPricePerSession(),
            product.getSessionCount()
        );
    }
}
