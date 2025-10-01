package com.solo.ptmatch.product.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductCategory;
import com.solo.ptmatch.product.infrastructure.ProductRepository;
import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.*;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final ProductRepository productRepository;

    // 상품 등록
    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByTrainerId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Product product = productRepository.save(request.toEntity(trainerProfile));
        return ProductCreateResponse.from(product);
    }

    // 상품 목록 조회
    @Transactional
    public List<ProductSummaryResponse> getProducts(ProductSearchRequest request) {

        Sort sort = createSort(request.sort());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Product> products = productRepository.searchByTitleAndCategoryAndPrice(
                request.titleKeyword(),
                ProductCategory.valueOf(request.category()),
                BigDecimal.valueOf(request.minPrice()),
                BigDecimal.valueOf(request.maxPrice()),
                pageable
        );

        return products.getContent().stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }

    // 상품 상세 조회
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

    private Sort createSort(String sortString) {
        if (sortString == null || sortString.isBlank()) {
            return Sort.unsorted();
        }

        if (!sortString.contains("_")) {
            return switch (sortString) {
                case "POPULAR" -> Sort.by(Sort.Direction.DESC, "likesCount");
                case "PRICE_ASC" -> Sort.by(Sort.Direction.ASC, "pricePerSession");
                case "PRICE_DESC" -> Sort.by(Sort.Direction.DESC, "pricePerSession");
                case "RECENT" -> Sort.by(Sort.Direction.DESC, "createdAt");
                default -> Sort.unsorted();
            };
        }

        String[] parts = sortString.split("_");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromString(parts[1])
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
