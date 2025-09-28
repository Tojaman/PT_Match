package com.solo.ptmatch.product.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "PT 상품 상세 응답")
public record ProductDetailResponse(
    @Schema(description = "상품 ID", example = "1")
    Long productId,
    @Schema(description = "상품명")
    String name,
    @Schema(description = "상품 설명")
    String description,
    @Schema(description = "카테고리")
    String category,
    @Schema(description = "총 가격")
    BigDecimal totalPrice,
    @Schema(description = "세션 수")
    int sessionCount,
    @Schema(description = "좋아요 수")
    Long likesCount,
    @Schema(description = "트레이너 정보")
    TrainerInfo trainerInfo,
    @Schema(description = "상품 이미지 목록")
    List<ImageInfo> images,
    @Schema(description = "상품 리뷰 목록")
    List<ReviewInfo> reviews
) {

    @Schema(description = "연결된 트레이너 정보")
    public record TrainerInfo(
        @Schema(description = "트레이너 ID", example = "15")
        Long trainerId,
        @Schema(description = "트레이너 이름")
        String trainerName,
        @Schema(description = "헬스장명")
        String gymName
    ) {
    }

    @Schema(description = "상품 이미지 정보")
    public record ImageInfo(
        @Schema(description = "이미지 URL")
        String imageUrl,
        @Schema(description = "노출 순서")
        int displayOrder
    ) {
    }

    @Schema(description = "리뷰 요약 정보")
    public record ReviewInfo(
        @Schema(description = "리뷰 ID", example = "1")
        Long reviewId,
        @Schema(description = "리뷰 작성자 이름")
        String reviewerName,
        @Schema(description = "평점", example = "5")
        int rating,
        @Schema(description = "리뷰 내용")
        String content
    ) {
    }
}
