package com.solo.ptmatch.product.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 이미지 수정 요청")
public record ImageUpdateRequest(
        @Schema(description = "이미지 ID", example = "101")
        @NotNull
        Long id,

        @Schema(description = "표시 순서", example = "1")
        @NotNull
        Integer displayOrder
) {
}
