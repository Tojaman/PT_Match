package com.solo.ptmatch.product.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PT 상품 삭제 응답")
public record ProductDeleteResponse(
    @Schema(description = "결과 메시지", example = "상품이 삭제되었습니다.")
    String message
) {
}
