package com.solo.ptmatch.product.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductCategory;
import com.solo.ptmatch.product.domain.ProductImage;
import com.solo.ptmatch.product.infrastructure.ProductImageRepository;
import com.solo.ptmatch.product.infrastructure.ProductRepository;
import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.ImageInfo;
import com.solo.ptmatch.product.presentation.response.ProductCreateResponse;
import com.solo.ptmatch.product.presentation.response.ProductDeleteResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import com.solo.ptmatch.product.presentation.response.ProductUpdateResponse;
import com.solo.ptmatch.product.presentation.response.ReviewInfo;
import com.solo.ptmatch.product.presentation.response.TrainerInfo;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;

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

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));
        TrainerInfo trainerInfo = TrainerInfo.from(product.getTrainerProfile());

        List<ImageInfo> productImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .map(ImageInfo::from)
                .toList();

        List<ReviewInfo> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(product.getId())
                .stream()
                .map(ReviewInfo::from)
                .toList();

        return ProductDetailResponse.from(product, trainerInfo, productImages, reviews);
    }

    // 상품 수정
    @Transactional
    public ProductUpdateResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        // 이미지 추가, 삭제 및 순서 재정렬
        updateImage(request.images(), product);

        product.update(
                request.title(),
                request.description(),
                request.category(),
                request.pricePerSession(),
                request.sessionCount()
        );

        List<ImageInfo> responseImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                .map(ImageInfo::from)
                .toList();

        return new ProductUpdateResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getCategory().name(),
                product.getPricePerSession(),
                product.getSessionCount(),
                responseImages
        );
    }

    // 상품 품절 처리
    @Transactional
    public void deactivateProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        product.deactivate();
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

    public void updateImage(List<ImageInfo> requestedImages, Product product) {
        if (requestedImages != null) {
            List<ProductImage> currentImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());

            // 현재 존재하는 이미지 Map
            Map<Long, ProductImage> currentImageMap = currentImages.stream()
                    .collect(Collectors.toMap(ProductImage::getId, image -> image));

            // 유지할 이미지 Set
            Set<Long> requestedIds = requestedImages.stream()
                    .map(ImageInfo::id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 유지할 이미지를 제외한 나머지 이미지 삭제
            List<ProductImage> imagesToDelete = currentImages.stream()
                    .filter(image -> !requestedIds.contains(image.getId()))
                    .toList();
            if (!imagesToDelete.isEmpty()) {
                productImageRepository.deleteAll(imagesToDelete);
            }

            List<ProductImage> imagesToCreate = new ArrayList<>();

            // 요청된 이미지 처리(업데이트 또는 생성)
            for (int index = 0; index < requestedImages.size(); index++) {
                ImageInfo imageInfo = requestedImages.get(index);

                if (imageInfo.id() != null) { // 기존 이미지 업데이트
                    ProductImage existingImage = currentImageMap.get(imageInfo.id());
                    existingImage.updateDisplayOrder(imageInfo.displayOrder()); // Dirty Checking
                } else { // 새로운 이미지 생성
                    ProductImage newImage = ProductImage.create(product, imageInfo.imageUrl(), imageInfo.displayOrder());
                    imagesToCreate.add(newImage);
                }
            }
            // 새로운 이미지 생성
            if (!imagesToCreate.isEmpty()) {
                productImageRepository.saveAll(imagesToCreate);
            }
        }
    }
}
