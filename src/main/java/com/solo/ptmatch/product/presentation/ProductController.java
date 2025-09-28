package com.solo.ptmatch.product.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.product.application.ProductService;
import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.ProductCreateResponse;
import com.solo.ptmatch.product.presentation.response.ProductDeleteResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductLikeToggleResponse;
import com.solo.ptmatch.product.presentation.response.ProductListResponse;
import com.solo.ptmatch.product.presentation.response.ProductUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "PT 상품 등록", description = "트레이너가 신규 PT 상품을 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 등록 성공")
    @PostMapping
    public ApiResponse<ProductCreateResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductCreateResponse response = productService.createProduct(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "PT 상품 목록 조회", description = "정렬/필터 조건으로 상품 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    @GetMapping
    public ApiResponse<ProductListResponse> getProducts(ProductSearchRequest request) {
        ProductListResponse response = productService.getProducts(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "PT 상품 상세 조회", description = "단일 PT 상품의 상세 정보를 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 상세 조회 성공")
    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable Long productId) {
        ProductDetailResponse response = productService.getProductDetail(productId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "PT 상품 수정", description = "기존 PT 상품 정보를 수정한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 수정 성공")
    @PutMapping("/{productId}")
    public ApiResponse<ProductUpdateResponse> updateProduct(
        @PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductUpdateResponse response = productService.updateProduct(productId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "PT 상품 삭제", description = "기존 PT 상품을 삭제한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 삭제 성공")
    @DeleteMapping("/{productId}")
    public ApiResponse<ProductDeleteResponse> deleteProduct(@PathVariable Long productId) {
        ProductDeleteResponse response = productService.deleteProduct(productId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "PT 상품 좋아요 토글", description = "사용자가 PT 상품 좋아요를 토글한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 토글 성공")
    @PostMapping("/{productId}/like")
    public ApiResponse<ProductLikeToggleResponse> toggleLike(@PathVariable Long productId) {
        ProductLikeToggleResponse response = productService.toggleLike(productId);
        return ApiResponse.success(response);
    }
}
