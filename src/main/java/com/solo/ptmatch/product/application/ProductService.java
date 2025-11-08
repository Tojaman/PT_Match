package com.solo.ptmatch.product.application;

import com.solo.ptmatch.common.aop.LogExecutionTime;
import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.common.storage.ObjectStorageService;
import com.solo.ptmatch.common.storage.PresignedUpload;
import com.solo.ptmatch.common.storage.PresignedUploadCommand;
import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.domain.ProductCategory;
import com.solo.ptmatch.product.domain.ProductImage;
import com.solo.ptmatch.product.infrastructure.ProductImageRepository;
import com.solo.ptmatch.product.infrastructure.ProductLikeRepository;
import com.solo.ptmatch.product.infrastructure.ProductRepository;
import com.solo.ptmatch.product.presentation.request.ImageUpdateRequest;
import com.solo.ptmatch.product.presentation.request.PresignedUrlRequest;
import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.ImageInfo;
import com.solo.ptmatch.product.presentation.response.PresignedUrlResponse;
import com.solo.ptmatch.product.presentation.response.ProductCreateResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import com.solo.ptmatch.product.presentation.response.ProductUpdateResponse;
import com.solo.ptmatch.product.presentation.response.TrainerInfo;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductLikeRepository productLikeRepository;
    private final ObjectStorageService objectStorageService;

    // 상품 등록 - 상품 목록으로 이동(이미지는 응답 데이터에 포함X)
    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Product product = productRepository.save(request.toEntity(trainerProfile));
        List<ProductImage> productImage = request.images().stream()
                        .map(image -> ProductImage.create(product, image.imageUrl(), image.displayOrder()))
                        .toList();
        productImageRepository.saveAll(productImage);
        return ProductCreateResponse.from(product);
    }

    // 상품 수정
    @Transactional
    public ProductUpdateResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        // 이미지 추가, 삭제 및 순서 재정렬
        updateImage(request, product);

        product.update(
                request.title(),
                request.description(),
                request.category(),
                request.pricePerSession(),
                request.sessionCount()
        );

        List<ImageInfo> responseImages = productImageRepository.findAllByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                .map(ImageInfo::from)
                .toList();

        return ProductUpdateResponse.from(product, responseImages);
    }

    @Transactional(readOnly = true)
    public PresignedUrlResponse issuePresignedUrl(PresignedUrlRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        PresignedUploadCommand command = new PresignedUploadCommand(
                user.getId(),
                request.fileName(),
                request.contentType(),
                "productImages"
        );
        PresignedUpload upload = objectStorageService.issuePresignedUpload(command);
        return PresignedUrlResponse.from(upload);
    }

    // 상품 품절 처리
    @Transactional
    public void deactivateProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        product.deactivate();
    }

    // 상품 목록 조회
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getProducts(ProductSearchRequest request, Pageable pageable) {

        Page<Product> products = productRepository.searchByTitleAndCategoryAndPrice(
                request.titleKeyword(),
                ProductCategory.valueOf(request.category()),
                BigDecimal.valueOf(request.minPrice()),
                BigDecimal.valueOf(request.maxPrice()),
                pageable
        );

        return products.map(ProductSummaryResponse::from);
    }

    // 상품 상세 조회
    @LogExecutionTime
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        TrainerInfo trainerInfo = TrainerInfo.from(product.getTrainerProfile());
        List<ImageInfo> productImages = productImageRepository.findAllByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .map(ImageInfo::from)
                .toList();

        return ProductDetailResponse.from(product, trainerInfo, productImages, product.getLikesCount());
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

    public void updateImage(ProductUpdateRequest request, Product product) {
        // 1. 이미지 삭제
        if (request.deletedImageIds() != null && !request.deletedImageIds().isEmpty()) {
            productImageRepository.deleteAllById(request.deletedImageIds());
        }

        // 2. 이미지 추가
        if (request.newImages() != null && !request.newImages().isEmpty()) {
            List<ProductImage> newImages = request.newImages().stream()
                    .map(image -> ProductImage.create(product, image.imageUrl(), image.displayOrder()))
                    .toList();
            productImageRepository.saveAll(newImages);
        }

        // 3. 이미지 순서 업데이트
        if (request.updatedImages() != null && !request.updatedImages().isEmpty()) {
            Map<Long, ProductImage> currentImageMap = productImageRepository.findAllByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                    .collect(Collectors.toMap(ProductImage::getId, image -> image));

            // 3.1 이미지 순서 재정렬
            for (ImageUpdateRequest imageUpdate : request.updatedImages()) {
                ProductImage image = currentImageMap.get(imageUpdate.id());
                if (image != null) {
                    image.updateDisplayOrder(imageUpdate.displayOrder());
                }
            }
        }
    }
}
