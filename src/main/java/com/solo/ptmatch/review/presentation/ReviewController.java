package com.solo.ptmatch.review.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.common.response.PageResponse;
import com.solo.ptmatch.review.application.ReviewService;
import com.solo.ptmatch.review.presentation.request.ReviewCreateRequest;
import com.solo.ptmatch.review.presentation.request.ReviewUpdateRequest;
import com.solo.ptmatch.review.presentation.response.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "후기 작성", description = "사용자가 매칭에 대한 후기를 작성한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "후기 작성 성공")
    @PostMapping("/reviews")
    public ApiResponse<ReviewCreateResponse> createReview(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewCreateResponse response = reviewService.createReview(email, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "후기 수정", description = "사용자가 작성한 후기를 수정한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "후기 수정 성공")
    @PatchMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewUpdateResponse> updateReview(
        @AuthenticationPrincipal(expression = "username") String email,
        @PathVariable Long reviewId,
        @Valid @RequestBody ReviewUpdateRequest request
    ) {
        ReviewUpdateResponse response = reviewService.updateReview(email, reviewId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "후기 삭제", description = "사용자가 작성한 후기를 삭제한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "후기 삭제 성공")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
        @AuthenticationPrincipal(expression = "username") String email,
        @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(email, reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 후기 목록", description = "사용자가 작성한 후기 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 후기 목록 조회 성공")
    @GetMapping("/me/reviews")
    public ApiResponse<List<MyReviewSummaryResponse>> getMyReviews(
        @AuthenticationPrincipal(expression = "username") String email,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<MyReviewSummaryResponse> reviewPage = reviewService.getMyReviews(email, page, size);

        PageResponse pageResponse = PageResponse.from(reviewPage);
        List<MyReviewSummaryResponse> reviews = reviewPage.getContent();

        return ApiResponse.success(reviews, pageResponse);
    }

    @Operation(summary = "상품 후기 목록", description = "상품의 후기 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 후기 목록 조회 성공")
    @GetMapping("/products/{productId}/reviews")
    public ApiResponse<List<ProductReviewSummaryResponse>> getProductReviews(
        @PathVariable Long productId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size
    ) {
        Page<ProductReviewSummaryResponse> response = reviewService.getProductReviews(productId, page, size);

        PageResponse pageResponse = PageResponse.from(response);
        List<ProductReviewSummaryResponse> reviews = response.getContent();

        return ApiResponse.success(reviews, pageResponse);
    }
}