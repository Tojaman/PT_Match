package com.solo.ptmatch.product.application;

import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.ProductCreateResponse;
import com.solo.ptmatch.product.presentation.response.ProductDeleteResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductLikeToggleResponse;
import com.solo.ptmatch.product.presentation.response.ProductListResponse;
import com.solo.ptmatch.product.presentation.response.ProductUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

    public ProductCreateResponse createProduct(ProductCreateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ProductListResponse getProducts(ProductSearchRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ProductDetailResponse getProductDetail(Long productId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ProductUpdateResponse updateProduct(Long productId, ProductUpdateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ProductDeleteResponse deleteProduct(Long productId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ProductLikeToggleResponse toggleLike(Long productId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
