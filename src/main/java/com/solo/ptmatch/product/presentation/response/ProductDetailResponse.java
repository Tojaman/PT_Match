package com.solo.ptmatch.product.presentation.response;

import com.solo.ptmatch.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "PT 상품 상세 응답")
public record ProductDetailResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,
    @Schema(description = "상품명")
    String title,
    @Schema(description = "상품 설명")
    String description,
    @Schema(description = "카테고리")
    String category,
    @Schema(description = "세션 당 가격")
    BigDecimal price,
    @Schema(description = "세션 수")
    int sessionCount,
    @Schema(description = "트레이너 정보")
    TrainerInfo trainerInfo,
    @Schema(description = "좋아요 수")
    long likesCount,
    @Schema(description = "상품 이미지 목록")
    List<ImageInfo> images
) {

    public static ProductDetailResponse from(Product product,
                                             TrainerInfo trainerInfo,
                                             List<ImageInfo> imageInfos,
                                             long likesCount) {
        return new ProductDetailResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getCategory().name(),
                product.getPricePerSession(),
                product.getSessionCount(),
                trainerInfo,
                likesCount,
                imageInfos
        );
    }
}
