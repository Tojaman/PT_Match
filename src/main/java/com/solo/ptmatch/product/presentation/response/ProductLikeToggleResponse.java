package com.solo.ptmatch.product.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PT 상품 좋아요 토글 응답")
public record ProductLikeToggleResponse(
    @Schema(description = "좋아요 여부", example = "true")
    boolean liked,
    @Schema(description = "결과 메시지", example = "좋아요 상태가 변경되었습니다.")
    String message
) {
}
