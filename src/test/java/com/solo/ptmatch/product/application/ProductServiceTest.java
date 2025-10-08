package com.solo.ptmatch.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import com.solo.ptmatch.product.presentation.response.ProductUpdateResponse;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final String TRAINER_EMAIL = "trainer@test.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainerProfileRepository trainerProfileRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    private User trainerUser;
    private TrainerProfile trainerProfile;

    @BeforeEach
    void setUp() {
        trainerUser = User.create(TRAINER_EMAIL, "encoded", "김트레이너", Role.TRAINER);
        ReflectionTestUtils.setField(trainerUser, "id", 101L);

        trainerProfile = TrainerProfile.create(
            trainerUser,
            "10년 경력 전문 트레이너",
            10,
            Specialty.DIET,
            "서울 강남구 헬스장",
            "https://example.com/profile.jpg"
        );
        ReflectionTestUtils.setField(trainerProfile, "id", 201L);
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("트레이너 프로필이 존재하면 상품을 저장하고 응답을 반환한다")
        void createProduct_success() {

            List<ImageInfo> images = List.of(
                new ImageInfo(null, "https://example.com/1.jpg", 0),
                new ImageInfo(null, "https://example.com/2.jpg", 1)
            );
            // given
            ProductCreateRequest request = new ProductCreateRequest(
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10,
                images
            );

            Product savedProduct = Product.create(
                trainerProfile,
                request.title(),
                request.description(),
                request.category(),
                request.pricePerSession(),
                request.sessionCount()
            );
            ReflectionTestUtils.setField(savedProduct, "id", 301L);

            when(userRepository.findByEmail(TRAINER_EMAIL)).thenReturn(Optional.of(trainerUser));
            when(trainerProfileRepository.findByUserId(trainerUser.getId())).thenReturn(Optional.of(trainerProfile));
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // when
            ProductCreateResponse response = productService.createProduct(request, TRAINER_EMAIL);

            // then
            assertThat(response.productId()).isEqualTo(301L);
            assertThat(response.name()).isEqualTo("PT 10회 패키지");
            assertThat(response.pricePerSession()).isEqualByComparingTo("50000");
            assertThat(response.sessionCount()).isEqualTo(10);

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());

            // productRepository.save() 검증
            Product captured = productCaptor.getValue();
            assertThat(captured.getTitle()).isEqualTo("PT 10회 패키지");
            assertThat(captured.getTrainerProfile()).isSameAs(trainerProfile);
        }

        @Test
        @DisplayName("사용자를 찾지 못하면 USER_NOT_FOUND 예외를 던진다")
        void createProduct_whenUserNotFound() {
            // given
            List<ImageInfo> images = List.of(
                    new ImageInfo(null, "https://example.com/1.jpg", 0),
                    new ImageInfo(null, "https://example.com/2.jpg", 1)
            );

            ProductCreateRequest request = new ProductCreateRequest(
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10,
                images
            );

            when(userRepository.findByEmail(TRAINER_EMAIL)).thenReturn(Optional.empty()); // 사용자 없음

            // when
            GlobalException exception = assertThrows(GlobalException.class,
                () -> productService.createProduct(request, TRAINER_EMAIL));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(userRepository).findByEmail(TRAINER_EMAIL);
            verify(trainerProfileRepository, times(0)).findByUserId(anyLong());
            verify(productRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("트레이너 프로필이 없으면 TRAINER_PROFILE_NOT_FOUND 예외를 던진다")
        void createProduct_whenTrainerProfileNotFound() {
            // given
            List<ImageInfo> images = List.of(
                    new ImageInfo(null, "https://example.com/1.jpg", 0),
                    new ImageInfo(null, "https://example.com/2.jpg", 1)
            );

            ProductCreateRequest request = new ProductCreateRequest(
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10,
                images
            );

            when(userRepository.findByEmail(TRAINER_EMAIL)).thenReturn(Optional.of(trainerUser));
            when(trainerProfileRepository.findByUserId(trainerUser.getId())).thenReturn(Optional.empty()); // 프로필 없음

            // when
            GlobalException exception = assertThrows(GlobalException.class,
                () -> productService.createProduct(request, TRAINER_EMAIL));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TRAINER_PROFILE_NOT_FOUND);
            verify(trainerProfileRepository).findByUserId(eq(trainerUser.getId()));
            verify(productRepository, times(0)).save(any());
        }
    }

    @Nested
    @DisplayName("getProducts")
    class GetProducts {

        @Test
        @DisplayName("검색 조건을 전달하면 상품 요약 목록을 반환한다")
        void getProducts_success() {
            // given
            ProductSearchRequest request = new ProductSearchRequest(
                "PT",
                "POPULAR",
                ProductCategory.MUSCLE_GAIN.name(),
                100000,
                500000,
                0,
                10
            );

            Product product = Product.create(
                trainerProfile,
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10
            );
            ReflectionTestUtils.setField(product, "id", 501L);
            ReflectionTestUtils.setField(product, "likesCount", 150);

            when(productRepository.searchByTitleAndCategoryAndPrice(
                eq("PT"),
                eq(ProductCategory.MUSCLE_GAIN),
                eq(BigDecimal.valueOf(100000)),
                eq(BigDecimal.valueOf(500000)),
                any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1));

            // when
            Page<ProductSummaryResponse> responses = productService.getProducts(request);

            // then
            assertThat(responses.getTotalElements()).isEqualTo(1);
            assertThat(responses.getContent()).hasSize(1);
            ProductSummaryResponse summary = responses.getContent().get(0);
            assertThat(summary.productId()).isEqualTo(501L);
            assertThat(summary.title()).isEqualTo("PT 10회 패키지");
            assertThat(summary.totalPrice()).isEqualByComparingTo("500000");
            assertThat(summary.trainerName()).isEqualTo("김트레이너");
            assertThat(summary.category()).isEqualTo(ProductCategory.MUSCLE_GAIN.name());
            assertThat(summary.likesCount()).isEqualTo(150L);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(productRepository).searchByTitleAndCategoryAndPrice(
                eq("PT"),
                eq(ProductCategory.MUSCLE_GAIN),
                eq(BigDecimal.valueOf(100000)),
                eq(BigDecimal.valueOf(500000)),
                pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
            Sort.Order sortOrder = capturedPageable.getSort().getOrderFor("likesCount");
            assertThat(sortOrder).isNotNull();
            assertThat(sortOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("지원하지 않는 카테고리를 전달하면 예외를 던진다")
        void getProducts_invalidCategory() {
            // given
            ProductSearchRequest request = new ProductSearchRequest(
                "PT",
                "pricePerSession_DESC",
                "UNKNOWN",
                100000,
                500000,
                0,
                10
            );

            // when & then
            assertThrows(IllegalArgumentException.class, () -> productService.getProducts(request));
            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("getProductDetail")
    class GetProductDetail {

        @Test
        @DisplayName("상품 상세 정보를 조회하면 이미지와 리뷰를 포함해 반환한다")
        void getProductDetail_success() {
            // given
            Long productId = 301L;
            Product product = Product.create(
                trainerProfile,
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10
            );
            ReflectionTestUtils.setField(product, "id", productId);

            ProductImage firstImage = ProductImage.create(product, "https://example.com/1.jpg", 0);

            User reviewer = User.create("member@test.com", "encoded", "홍길동", Role.USER);
            ReflectionTestUtils.setField(reviewer, "id", 401L);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productImageRepository.findAllByProductIdOrderByDisplayOrderAsc(productId))
                .thenReturn(List.of(firstImage));

            // when
            ProductDetailResponse response = productService.getProductDetail(productId);

            // then
            assertThat(response.productId()).isEqualTo(productId);
            assertThat(response.title()).isEqualTo("PT 10회 패키지");
            assertThat(response.price()).isEqualByComparingTo("50000");
            assertThat(response.trainerInfo().trainerId()).isEqualTo(trainerProfile.getId());

            assertThat(response.images()).hasSize(1);
            assertThat(response.images().get(0).imageUrl()).isEqualTo("https://example.com/1.jpg");
            assertThat(response.images().get(0).displayOrder()).isZero();

            verify(productRepository).findById(productId);
            verify(productImageRepository).findAllByProductIdOrderByDisplayOrderAsc(productId);
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("상품 정보를 업데이트하고 이미지 변경을 반영한다")
        void updateProduct_success() {
            // given
            Long productId = 701L;
            // 기존 상품 엔티티를 준비한다
            Product product = Product.create(
                trainerProfile,
                "원래 상품",
                "초기 설명",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("45000"),
                8
            );
            ReflectionTestUtils.setField(product, "id", productId);

            // 유지해야 하는 기존 이미지와 삭제 대상 이미지를 구성한다
            ProductImage existingImage = ProductImage.create(product, "https://example.com/existing-1.jpg", 0);
            ReflectionTestUtils.setField(existingImage, "id", 801L);

            ProductImage imageToDelete = ProductImage.create(product, "https://example.com/existing-2.jpg", 1);
            ReflectionTestUtils.setField(imageToDelete, "id", 802L);

            // 제목, 설명 등 상품 정보와 이미지 변경 요청을 정의한다
            ProductUpdateRequest request = new ProductUpdateRequest(
                "수정된 상품",
                "새로운 설명",
                ProductCategory.WEIGHT_LOSS,
                new BigDecimal("55000"),
                12,
                List.of(
                    new ImageInfo(existingImage.getId(), existingImage.getImageUrl(), 2),
                    new ImageInfo(null, "https://example.com/new-1.jpg", 1)
                )
            );

            // 새 이미지 저장 결과를 추적하기 위한 참조를 준비한다
            AtomicReference<ProductImage> newlyCreatedImageRef = new AtomicReference<>();
            AtomicInteger imageQueryCount = new AtomicInteger(0);

            // 상품과 이미지 목록을 조회하는 저장소 동작을 가짜로 구성한다
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productImageRepository.findAllByProductIdOrderByDisplayOrderAsc(productId))
                .thenAnswer(invocation -> {
                    // 저장소가 최초 조회 시 기존 데이터, 이후에는 변경된 데이터를 반환하도록 시뮬레이션
                    if (imageQueryCount.getAndIncrement() == 0) {
                        return List.of(existingImage, imageToDelete);
                    }
                    ProductImage newImage = newlyCreatedImageRef.get();
                    return newImage == null
                        ? List.of(existingImage)
                        : List.of(newImage, existingImage);
                });
            // 새 이미지를 저장하고 ID가 채워진다고 가정한다
            when(productImageRepository.saveAll(anyList())).thenAnswer(invocation -> {
                @SuppressWarnings("unchecked")
                List<ProductImage> newImages = (List<ProductImage>) invocation.getArgument(0);
                for (int index = 0; index < newImages.size(); index++) {
                    ProductImage image = newImages.get(index);
                    ReflectionTestUtils.setField(image, "id", 900L + index);
                }
                if (!newImages.isEmpty()) {
                    newlyCreatedImageRef.set(newImages.get(0));
                }
                return newImages;
            });

            // when
            ProductUpdateResponse response = productService.updateProduct(productId, request);

            // then
            // 상품 엔티티의 필드가 요청값으로 갱신됐는지 확인한다
            assertThat(product.getTitle()).isEqualTo("수정된 상품");
            assertThat(product.getDescription()).isEqualTo("새로운 설명");
            assertThat(product.getCategory()).isEqualTo(ProductCategory.WEIGHT_LOSS);
            assertThat(product.getPricePerSession()).isEqualByComparingTo("55000");
            assertThat(product.getSessionCount()).isEqualTo(12);

            // 기존 이미지는 displayOrder만 업데이트되어야 한다
            assertThat(existingImage.getDisplayOrder()).isEqualTo(2);

            ProductImage newlyCreatedImage = newlyCreatedImageRef.get();
            assertThat(newlyCreatedImage).isNotNull();

            // 저장소 호출이 예상대로 발생했는지 검증한다
            verify(productRepository).findById(productId);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ProductImage>> deleteCaptor = ArgumentCaptor.forClass(List.class);
            // 삭제 요청에는 제거 대상 이미지 하나만 포함되어야 한다
            verify(productImageRepository).deleteAll(deleteCaptor.capture());
            assertThat(deleteCaptor.getValue()).containsExactly(imageToDelete);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ProductImage>> saveCaptor = ArgumentCaptor.forClass(List.class);
            // 저장 요청에는 새 이미지 하나만 전달된다
            verify(productImageRepository).saveAll(saveCaptor.capture());
            List<ProductImage> savedImages = saveCaptor.getValue();
            assertThat(savedImages).hasSize(1);
            ProductImage capturedNewImage = savedImages.get(0);
            assertThat(capturedNewImage.getProduct()).isSameAs(product);
            assertThat(capturedNewImage.getImageUrl()).isEqualTo("https://example.com/new-1.jpg");
            assertThat(capturedNewImage.getDisplayOrder()).isEqualTo(1);
            assertThat(capturedNewImage.getId()).isEqualTo(900L);

            verify(productImageRepository, times(2)).findAllByProductIdOrderByDisplayOrderAsc(productId);

            // 응답 DTO에 변경된 정보와 이미지 목록이 반영되는지 확인한다
            assertThat(response.productId()).isEqualTo(productId);
            assertThat(response.title()).isEqualTo("수정된 상품");
            assertThat(response.description()).isEqualTo("새로운 설명");
            assertThat(response.category()).isEqualTo(ProductCategory.WEIGHT_LOSS.name());
            assertThat(response.pricePerSession()).isEqualByComparingTo("55000");
            assertThat(response.sessionCount()).isEqualTo(12);
            assertThat(response.images())
                .extracting(ImageInfo::id, ImageInfo::imageUrl, ImageInfo::displayOrder)
                .containsExactlyInAnyOrder(
                    tuple(existingImage.getId(), existingImage.getImageUrl(), 2),
                    tuple(newlyCreatedImage.getId(), newlyCreatedImage.getImageUrl(), newlyCreatedImage.getDisplayOrder())
                );
        }

        @Test
        @DisplayName("상품을 찾지 못하면 PRODUCT_NOT_FOUND 예외를 던진다")
        void updateProduct_whenProductNotFound() {
            // given
            ProductUpdateRequest request = new ProductUpdateRequest(
                "수정된 상품",
                "새로운 설명",
                ProductCategory.WEIGHT_LOSS,
                new BigDecimal("55000"),
                12,
                List.of()
            );

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // when
            GlobalException exception = assertThrows(GlobalException.class,
                () -> productService.updateProduct(999L, request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
            verify(productRepository).findById(999L);
            verifyNoInteractions(productImageRepository);
        }
    }
}
