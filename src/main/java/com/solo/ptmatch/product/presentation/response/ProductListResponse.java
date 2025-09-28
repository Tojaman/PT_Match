package com.solo.ptmatch.product.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "PT 상품 목록 응답")
public record ProductListResponse(
    @Schema(description = "상품 목록")
    List<ProductSummaryResponse> products
) {
}
