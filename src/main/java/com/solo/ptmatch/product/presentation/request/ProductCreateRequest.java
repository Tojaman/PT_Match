package com.solo.ptmatch.product.presentation.request;

import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductCategory;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "PT 상품 등록 요청")
public record ProductCreateRequest(
    @Schema(description = "상품명", example = "PT 10회 패키지")
    @NotBlank
    String title,

    @Schema(description = "상품 설명", example = "10회 집중 관리 프로그램")
    @NotBlank
    String description,

    @Schema(description = "카테고리", example = "DIET")
    @NotNull
    ProductCategory category,

    @Schema(description = "회당 가격", example = "50000")
    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    BigDecimal pricePerSession,

    @Schema(description = "세션 수", example = "10")
    @Min(1)
    int sessionCount,

    @Schema(description = "썸네일 URL", example = "https://example.com/thumb.jpg")
    @NotBlank
    String thumbnailUrl
) {
    public Product toEntity(TrainerProfile trainerProfile) {
        return Product.create(
            trainerProfile, // TrainerProfile will be set in the service layer
            title,
            description,
            category,
            pricePerSession,
            sessionCount,
            thumbnailUrl
        );
    }
}
