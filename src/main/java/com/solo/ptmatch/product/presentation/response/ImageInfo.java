package com.solo.ptmatch.product.presentation.response;

import com.solo.ptmatch.product.domain.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 이미지 정보")
public record ImageInfo(
    @Schema(description = "이미지 ID")
    Long id,
    @Schema(description = "이미지 URL")
    String imageUrl,
    @Schema(description = "노출 순서")
    int displayOrder
) {

    public static ImageInfo from(ProductImage productImage) {
        return new ImageInfo(productImage.getId(), productImage.getImageUrl(), productImage.getDisplayOrder());
    }
}
