package com.solo.ptmatch.product.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.product.application.ProductService;
import com.solo.ptmatch.product.domain.ProductCategory;
import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.*;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@WebMvcTest(ProductController.class)
class ProductContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("트레이너는 PT 상품을 성공적으로 등록한다")
    @WithMockUser(roles = "TRAINER", username = "trainer@test.com") // 스프링 시큐리티 권한 통과를 위해 TRAINER로 권한 설정
    void createProduct_succeeds_when_user_is_trainer() throws Exception {
        // given
        ProductCreateRequest requestPayload = new ProductCreateRequest(
            "PT 10회 패키지",
            "10회 집중 관리 프로그램",
                ProductCategory.MUSCLE_GAIN,
            new BigDecimal("50000"),
            10
        );

        ProductCreateResponse serviceResult = new ProductCreateResponse(1L, "PT 10회 패키지", new BigDecimal("50000"), 10);

        // createProduct() 메서드가 호출될 때 serviceResult를 반환하도록 설정
        given(productService.createProduct(any(ProductCreateRequest.class), any(String.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/products")
                .with(csrf()) // CSRF 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.productId").value(1L));

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductCreateRequest> requestCaptor = ArgumentCaptor.forClass(ProductCreateRequest.class);

        // 요청 파라미터 검증
        then(productService).should().createProduct(requestCaptor.capture(), emailCaptor.capture());
        assertThat(emailCaptor.getValue()).isEqualTo("trainer@test.com");
        assertThat(requestCaptor.getValue().title()).isEqualTo("PT 10회 패키지");
        assertThat(requestCaptor.getValue().category()).isEqualTo(ProductCategory.MUSCLE_GAIN);
    }

    @Test
    @DisplayName("일반 유저는 PT 상품을 등록할 수 없다")
    @WithMockUser(roles = "USER")
    void createProduct_fails_when_user_is_not_trainer() throws Exception {
        // given
        ProductCreateRequest requestPayload = new ProductCreateRequest(
            "PT 10회 패키지",
            "10회 집중 관리 프로그램",
                ProductCategory.MUSCLE_GAIN,
            new BigDecimal("50000"),
            10
        );

        // when
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PT 상품 목록 조회 시 필터 조건을 전달하고 올바른 결과를 받는다")
    @WithMockUser
    void getProductsReturnsList() throws Exception {
        // given
        PageImpl<ProductSummaryResponse> serviceResult = new PageImpl<>(
            List.of(
                new ProductSummaryResponse(
                    2L,
                    "PT 20회",
                    new BigDecimal("900000"),
                    "최전문",
                    "DIET",
                    150L
                )
            ),
            PageRequest.of(0, 10),
            1
        );

        given(productService.getProducts(any(ProductSearchRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/products")
                .param("sort", "POPULAR")
                .param("category", "DIET")
                .param("minPrice", "100000")
                .param("maxPrice", "500000")
                .param("page", "0")
                .param("size", "10"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data[0].productId").value(2))
            .andExpect(jsonPath("$.data[0].title").value("PT 20회"))
            .andExpect(jsonPath("$.data[0].totalPrice").value(900000))
            .andExpect(jsonPath("$.data[0].trainerName").value("최전문"))
            .andExpect(jsonPath("$.data[0].category").value("DIET"))
            .andExpect(jsonPath("$.data[0].likesCount").value(150))
            .andExpect(jsonPath("$.pageResponse.page").value(0))
            .andExpect(jsonPath("$.pageResponse.size").value(10))
            .andExpect(jsonPath("$.pageResponse.totalElements").value(1))
            .andExpect(jsonPath("$.pageResponse.totalPages").value(1))
            .andExpect(jsonPath("$.pageResponse.hasNext").value(false));

        ArgumentCaptor<ProductSearchRequest> requestCaptor = ArgumentCaptor.forClass(ProductSearchRequest.class);
        then(productService).should().getProducts(requestCaptor.capture());

        ProductSearchRequest captured = requestCaptor.getValue();
        assertThat(captured.sort()).isEqualTo("POPULAR");
        assertThat(captured.category()).isEqualTo("DIET");
        assertThat(captured.minPrice()).isEqualTo(100000);
        assertThat(captured.maxPrice()).isEqualTo(500000);
        assertThat(captured.page()).isEqualTo(0);
        assertThat(captured.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("PT 상품 상세 조회 시 상세 정보를 반환한다")
    @WithMockUser
    void getProductReturnsDetail() throws Exception {
        // given
        ProductDetailResponse serviceResult = new ProductDetailResponse(
            1L,
            "PT 10회 집중관리",
            "10회 세션으로 구성된 맞춤형 집중 관리 프로그램입니다.",
            "DIET",
            new BigDecimal("50000"),
            10,
            new TrainerInfo(15L, "박전문", "피트니스 센터 강남점"),
            List.of(new ImageInfo(1L, "https://example.com/image_main.jpg", 0)),
            List.of(new ReviewInfo(1L, "김회원", 5, "덕분에 목표 달성했습니다!"))
        );

        given(productService.getProductDetail(1L)).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/products/{productId}", 1L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.productId").value(1))
            .andExpect(jsonPath("$.data.title").value("PT 10회 집중관리"))
            .andExpect(jsonPath("$.data.description").value("10회 세션으로 구성된 맞춤형 집중 관리 프로그램입니다."))
            .andExpect(jsonPath("$.data.category").value("DIET"))
            .andExpect(jsonPath("$.data.price").value(50000))
            .andExpect(jsonPath("$.data.sessionCount").value(10))
            .andExpect(jsonPath("$.data.trainerInfo.trainerId").value(15))
            .andExpect(jsonPath("$.data.trainerInfo.trainerName").value("박전문"))
            .andExpect(jsonPath("$.data.trainerInfo.gymName").value("피트니스 센터 강남점"))
            .andExpect(jsonPath("$.data.images[0].id").value(1))
            .andExpect(jsonPath("$.data.images[0].imageUrl").value("https://example.com/image_main.jpg"))
            .andExpect(jsonPath("$.data.images[0].displayOrder").value(0))
            .andExpect(jsonPath("$.data.reviews[0].reviewId").value(1))
            .andExpect(jsonPath("$.data.reviews[0].reviewerName").value("김회원"))
            .andExpect(jsonPath("$.data.reviews[0].rating").value(5))
            .andExpect(jsonPath("$.data.reviews[0].content").value("덕분에 목표 달성했습니다!"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(productService).should().getProductDetail(1L);
    }

    @Test
    @DisplayName("PT 상품 수정 시 갱신된 정보를 반환한다")
    @WithMockUser(roles = "TRAINER", username = "trainer@test.com")
    void updateProductReturnsUpdatedSummary() throws Exception {
        // given
        ProductUpdateRequest requestPayload = new ProductUpdateRequest(
            "PT 집중관리 12회",
            "12회 구성으로 업그레이드된 프로그램",
                ProductCategory.MUSCLE_GAIN,
            new BigDecimal("60000"),
            12,
            List.of(
                new ImageInfo(1L, "https://example.com/existing-thumb.jpg", 0),
                new ImageInfo(null, "https://example.com/new-thumb.jpg", 1)
            )
        );

        ProductUpdateResponse serviceResult = new ProductUpdateResponse(
            1L,
            "PT 집중관리 12회",
            "12회 구성으로 업그레이드된 프로그램",
            "STRENGTH",
            new BigDecimal("60000"),
            12,
            List.of(new ImageInfo(1L, "https://example.com/existing-thumb.jpg", 0))
        );

        given(productService.updateProduct(any(Long.class), any(ProductUpdateRequest.class)))
            .willReturn(serviceResult);

        // when
        mockMvc.perform(put("/api/products/{productId}", 1L)
                .with(csrf()) // CSRF 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.productId").value(1))
            .andExpect(jsonPath("$.data.title").value("PT 집중관리 12회"))
            .andExpect(jsonPath("$.data.pricePerSession").value(60000))
            .andExpect(jsonPath("$.data.sessionCount").value(12))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ProductUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ProductUpdateRequest.class);
        then(productService).should().updateProduct(idCaptor.capture(), requestCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(1L);
        ProductUpdateRequest captured = requestCaptor.getValue();
        assertThat(captured.title()).isEqualTo("PT 집중관리 12회");
        assertThat(captured.description()).isEqualTo("12회 구성으로 업그레이드된 프로그램");
        assertThat(captured.category()).isEqualTo(ProductCategory.MUSCLE_GAIN);
        assertThat(captured.pricePerSession()).isEqualByComparingTo("60000");
        assertThat(captured.sessionCount()).isEqualTo(12);
        assertThat(captured.images()).hasSize(2);
        ImageInfo newImage = captured.images().get(1);
        assertThat(newImage.id()).isNull();
        assertThat(newImage.imageUrl()).isEqualTo("https://example.com/new-thumb.jpg");
        assertThat(newImage.displayOrder()).isEqualTo(1);
    }
}
