package com.solo.ptmatch.product.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 좋아요 토글 결과")
public record ProductLikeToggleResponse(
    @Schema(description = "좋아요 여부", example = "true")
    boolean liked
) {
}
