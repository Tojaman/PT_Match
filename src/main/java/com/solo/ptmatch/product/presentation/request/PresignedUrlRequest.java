package com.solo.ptmatch.product.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "상품 이미지 업로드용 프리사인 URL 요청")
public record PresignedUrlRequest(
        @Schema(description = "원본 파일명", example = "thumbnail.png")
        @NotBlank
        String fileName,
        @Schema(description = "MIME 타입", example = "image/png")
        @NotBlank
        String contentType
) {
}
