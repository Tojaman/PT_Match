package com.solo.ptmatch.product.presentation.response;

import com.solo.ptmatch.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "PT 상품 요약")
public record ProductSummaryResponse(
    @Schema(description = "상품 ID", example = "2")
    Long productId,
    @Schema(description = "상품명")
    String title,
    @Schema(description = "총 가격 또는 대표 가격")
    BigDecimal totalPrice,
    @Schema(description = "트레이너 이름")
    String trainerName,
    @Schema(description = "카테고리")
    String category,
    @Schema(description = "좋아요 수")
    Long likesCount
) {

    public static ProductSummaryResponse from(Product product) {
        BigDecimal totalPrice = product.getPricePerSession()
            .multiply(BigDecimal.valueOf(product.getSessionCount()));
        return new ProductSummaryResponse(
            product.getId(),
            product.getTitle(),
            totalPrice,
            product.getTrainerProfile().getTrainer().getName(),
            product.getCategory().name(),
            (long) product.getLikesCount()
        );
    }
}
