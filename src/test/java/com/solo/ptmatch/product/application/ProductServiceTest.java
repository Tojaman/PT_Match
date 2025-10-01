package com.solo.ptmatch.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.solo.ptmatch.product.presentation.response.ProductCreateResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import com.solo.ptmatch.review.domain.Review;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
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
            // given
            ProductCreateRequest request = new ProductCreateRequest(
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10,
                "https://example.com/thumb.jpg"
            );

            Product savedProduct = Product.create(
                trainerProfile,
                request.title(),
                request.description(),
                request.category(),
                request.pricePerSession(),
                request.sessionCount(),
                request.thumbnailUrl()
            );
            ReflectionTestUtils.setField(savedProduct, "id", 301L);

            when(userRepository.findByEmail(TRAINER_EMAIL)).thenReturn(Optional.of(trainerUser));
            when(trainerProfileRepository.findByTrainerId(trainerUser.getId())).thenReturn(Optional.of(trainerProfile));
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
            ProductCreateRequest request = new ProductCreateRequest(
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10,
                "https://example.com/thumb.jpg"
            );

            when(userRepository.findByEmail(TRAINER_EMAIL)).thenReturn(Optional.empty()); // 사용자 없음

            // when
            GlobalException exception = assertThrows(GlobalException.class,
                () -> productService.createProduct(request, TRAINER_EMAIL));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(userRepository).findByEmail(TRAINER_EMAIL);
            verify(trainerProfileRepository, times(0)).findByTrainerId(anyLong());
            verify(productRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("트레이너 프로필이 없으면 TRAINER_PROFILE_NOT_FOUND 예외를 던진다")
        void createProduct_whenTrainerProfileNotFound() {
            // given
            ProductCreateRequest request = new ProductCreateRequest(
                "PT 10회 패키지",
                "체계적인 10회 세션",
                ProductCategory.MUSCLE_GAIN,
                new BigDecimal("50000"),
                10,
                "https://example.com/thumb.jpg"
            );

            when(userRepository.findByEmail(TRAINER_EMAIL)).thenReturn(Optional.of(trainerUser));
            when(trainerProfileRepository.findByTrainerId(trainerUser.getId())).thenReturn(Optional.empty()); // 프로필 없음

            // when
            GlobalException exception = assertThrows(GlobalException.class,
                () -> productService.createProduct(request, TRAINER_EMAIL));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TRAINER_PROFILE_NOT_FOUND);
            verify(trainerProfileRepository).findByTrainerId(eq(trainerUser.getId()));
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
                10,
                "https://example.com/thumb.jpg"
            );
            ReflectionTestUtils.setField(product, "id", 501L);
            ReflectionTestUtils.setField(product, "likesCount", 150);

            when(productRepository.searchByTitleAndCategoryAndPrice(
                eq("PT"),
                eq(ProductCategory.MUSCLE_GAIN),
                eq(BigDecimal.valueOf(100000)),
                eq(BigDecimal.valueOf(500000)),
                any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(product)));

            // when
            List<ProductSummaryResponse> responses = productService.getProducts(request);

            // then
            assertThat(responses).hasSize(1);
            ProductSummaryResponse summary = responses.get(0);
            assertThat(summary.productId()).isEqualTo(501L);
            assertThat(summary.title()).isEqualTo("PT 10회 패키지");
            assertThat(summary.totalPrice()).isEqualByComparingTo("500000");
            assertThat(summary.trainerName()).isEqualTo("김트레이너");
            assertThat(summary.category()).isEqualTo(ProductCategory.MUSCLE_GAIN.name());
            assertThat(summary.thumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
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
                10,
                "https://example.com/thumb.jpg"
            );
            ReflectionTestUtils.setField(product, "id", productId);

            ProductImage firstImage = ProductImage.create(product, "https://example.com/1.jpg", 0);

            User reviewer = User.create("member@test.com", "encoded", "홍길동", Role.USER);
            ReflectionTestUtils.setField(reviewer, "id", 401L);

            Review review = Review.create(reviewer, trainerProfile, 5, "전반적으로 만족했습니다.");
            ReflectionTestUtils.setField(review, "id", 501L);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId))
                .thenReturn(List.of(firstImage));
            when(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId))
                .thenReturn(List.of(review));

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

            assertThat(response.reviews()).hasSize(1);
            assertThat(response.reviews().get(0).reviewerName()).isEqualTo("홍길동");
            assertThat(response.reviews().get(0).rating()).isEqualTo(5);
            assertThat(response.reviews().get(0).content()).isEqualTo("전반적으로 만족했습니다.");

            verify(productRepository).findById(productId);
            verify(productImageRepository).findByProductIdOrderByDisplayOrderAsc(productId);
            verify(reviewRepository).findByProductIdOrderByCreatedAtDesc(productId);
        }
    }
}
