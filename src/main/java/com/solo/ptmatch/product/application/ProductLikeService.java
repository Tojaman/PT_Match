package com.solo.ptmatch.product.application;

import com.solo.ptmatch.common.aop.LogExecutionTime;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductLikeService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;

    @LogExecutionTime
    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class
            },
            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 100,
                    multiplier = 1.5,
                    random = true), // 재시도 간격 무작위성 추가(+- 50%) -> 재시도 시점 분산
            listeners = "retryLoggingListener"
    )
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
            changeProductLikeCount(product, 1);
            productLikeRepository.save(ProductLike.create(user, product));
            return new ProductLikeToggleResponse(true);
        }

        changeProductLikeCount(product, -1);
        productLikeRepository.delete(productLike);
        return new ProductLikeToggleResponse(false);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getLikedProducts(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        return productLikeRepository.findAllByUserIdWithProduct(user.getId(), pageable)
                .map(productLike -> ProductSummaryResponse.from(productLike.getProduct()));
    }

    private void changeProductLikeCount(Product product, int delta) {
        int updatedRows = productRepository.updateLikeCountWithVersion(product.getId(), delta, product.getVersion());
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Product.class, product.getId());
        }
    }

    @Recover
    public ProductLikeToggleResponse recoverLikeToggle(
            ObjectOptimisticLockingFailureException exception,
            Long productId,
            String userEmail
    ) {
        log.error(
                "상품 좋아요 토글 재시도 최종 실패: productId={}, userEmail={}, 예외={}",
                productId,
                userEmail,
                exception.getClass().getSimpleName()
        );
        throw GlobalException.of(ErrorCode.PRODUCT_LIKE_RETRY_FAILED);
    }
}
