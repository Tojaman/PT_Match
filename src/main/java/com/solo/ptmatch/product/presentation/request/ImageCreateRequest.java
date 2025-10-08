package com.solo.ptmatch.product.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 이미지 생성 요청")
public record ImageCreateRequest(
        @Schema(description = "이미지 URL", example = "http://example.com/new_image.jpg")
        @NotNull
        String imageUrl,

        @Schema(description = "표시 순서", example = "3")
        @NotNull
        Integer displayOrder
) {
}
