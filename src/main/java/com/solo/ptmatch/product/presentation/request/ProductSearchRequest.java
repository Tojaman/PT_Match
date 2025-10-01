package com.solo.ptmatch.product.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PT 상품 검색 조건")
public record ProductSearchRequest(
    @Schema(description = "제목 키워드", example = "다이어트")
    String titleKeyword,
    @Schema(description = "정렬 기준", example = "POPULAR")
    String sort,
    @Schema(description = "카테고리", example = "DIET")
    String category,
    @Schema(description = "최소 가격", example = "100000")
    Integer minPrice,
    @Schema(description = "최대 가격", example = "500000")
    Integer maxPrice,
    @Schema(description = "페이지 번호", example = "0")
    Integer page,
    @Schema(description = "페이지 크기", example = "10")
    Integer size
) {
}
