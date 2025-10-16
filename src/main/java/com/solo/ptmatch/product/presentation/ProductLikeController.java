package com.solo.ptmatch.product.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.common.response.PageResponse;
import com.solo.ptmatch.product.application.ProductLikeService;
import com.solo.ptmatch.product.presentation.response.ProductLikeToggleResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ProductLikeController {

    private final ProductLikeService productLikeService;

    @Operation(summary = "PT 상품 좋아요 토글", description = "사용자가 PT 상품 좋아요 상태를 토글한다")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/products/{productId}/like")
    public ResponseEntity<ApiResponse<ProductLikeToggleResponse>> toggleProductLike(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @PathVariable Long productId
    ) {
        ProductLikeToggleResponse response = productLikeService.toggleProductLike(productId, loggedInEmail);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "좋아요한 PT 상품 목록", description = "사용자가 좋아요한 PT 상품 목록을 조회한다")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me/likes/products")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getLikedProducts(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductSummaryResponse> response = productLikeService.getLikedProducts(loggedInEmail, pageable);
        return ResponseEntity.ok(ApiResponse.success(response.getContent(), PageResponse.from(response)));
    }
}
