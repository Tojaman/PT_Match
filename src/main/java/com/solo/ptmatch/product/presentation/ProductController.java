package com.solo.ptmatch.product.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.common.response.PageResponse;
import com.solo.ptmatch.product.application.ProductService;
import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "PT 상품 등록", description = "트레이너가 신규 PT 상품을 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 등록 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody ProductCreateRequest request) {
        ProductCreateResponse response = productService.createProduct(request, loggedInEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "PT 상품 수정", description = "기존 PT 상품 정보를 수정한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 수정 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductUpdateResponse>> updateProduct(
        @PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductUpdateResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "PT 상품 품절 처리", description = "기존 PT 상품을 품절 처리한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 품절 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable Long productId) {
        productService.deactivateProduct(productId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "PT 상품 목록 조회", description = "정렬/필터 조건으로 상품 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getProducts(
            @ModelAttribute ProductSearchRequest request,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductSummaryResponse> response = productService.getProducts(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(response.getContent(), PageResponse.from(response)));
    }

    @Operation(summary = "PT 상품 상세 조회", description = "단일 PT 상품의 상세 정보를 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 상세 조회 성공")
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long productId) {
        ProductDetailResponse response = productService.getProductDetail(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
