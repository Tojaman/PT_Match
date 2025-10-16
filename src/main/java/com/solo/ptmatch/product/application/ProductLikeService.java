package com.solo.ptmatch.product.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductLike;
import com.solo.ptmatch.product.infrastructure.ProductLikeRepository;
import com.solo.ptmatch.product.infrastructure.ProductRepository;
import com.solo.ptmatch.product.presentation.response.ProductLikeToggleResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductLikeService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public ProductLikeToggleResponse toggleProductLike(Long productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getTrainerProfile().getUser().getId().equals(user.getId())) {
            throw GlobalException.of(ErrorCode.SELF_PRODUCT_LIKE_NOT_ALLOWED);
        }

        ProductLike productLike = productLikeRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElse(null);

        if (productLike == null) {
            productLikeRepository.save(ProductLike.create(user, product));
            product.increaseLike();
            return new ProductLikeToggleResponse(true);
        }

        productLikeRepository.delete(productLike);
        product.decreaseLike();
        return new ProductLikeToggleResponse(false);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getLikedProducts(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        return productLikeRepository.findAllByUserIdWithProduct(user.getId(), pageable)
                .map(productLike -> ProductSummaryResponse.from(productLike.getProduct()));
    }
}
