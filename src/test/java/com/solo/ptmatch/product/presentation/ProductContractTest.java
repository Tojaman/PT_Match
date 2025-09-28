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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.product.application.ProductService;
import com.solo.ptmatch.product.presentation.request.ProductCreateRequest;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.request.ProductUpdateRequest;
import com.solo.ptmatch.product.presentation.response.ProductCreateResponse;
import com.solo.ptmatch.product.presentation.response.ProductDeleteResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse.ImageInfo;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse.ReviewInfo;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse.TrainerInfo;
import com.solo.ptmatch.product.presentation.response.ProductLikeToggleResponse;
import com.solo.ptmatch.product.presentation.response.ProductListResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import com.solo.ptmatch.product.presentation.response.ProductUpdateResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
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
    @DisplayName("PT 상품 등록 시 등록 결과를 반환한다")
    void createProductReturnsCreatedSummary() throws Exception {
        // given
        ProductCreateRequest requestPayload = new ProductCreateRequest(
            "PT 10회 패키지",
            "10회 집중 관리 프로그램",
            "DIET",
            new BigDecimal("50000"),
            10,
            "https://example.com/thumb.jpg"
        );

        ProductCreateResponse serviceResult = new ProductCreateResponse(1L, "PT 10회 패키지", new BigDecimal("50000"), 10);

        given(productService.createProduct(any(ProductCreateRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.productId").value(1))
            .andExpect(jsonPath("$.data.name").value("PT 10회 패키지"))
            .andExpect(jsonPath("$.data.pricePerSession").value(50000))
            .andExpect(jsonPath("$.data.sessionCount").value(10))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<ProductCreateRequest> requestCaptor = ArgumentCaptor.forClass(ProductCreateRequest.class);
        then(productService).should().createProduct(requestCaptor.capture());

        ProductCreateRequest captured = requestCaptor.getValue();
        assertThat(captured.name()).isEqualTo("PT 10회 패키지");
        assertThat(captured.description()).isEqualTo("10회 집중 관리 프로그램");
        assertThat(captured.category()).isEqualTo("DIET");
        assertThat(captured.pricePerSession()).isEqualByComparingTo("50000");
        assertThat(captured.sessionCount()).isEqualTo(10);
        assertThat(captured.thumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
    }

    @Test
    @DisplayName("PT 상품 목록 조회 시 필터 조건을 전달한다")
    void getProductsReturnsList() throws Exception {
        // given
        ProductListResponse serviceResult = new ProductListResponse(
            List.of(
                new ProductSummaryResponse(
                    2L,
                    "PT 20회",
                    new BigDecimal("900000"),
                    "최전문",
                    "DIET",
                    "https://example.com/thumb2.jpg",
                    150L
                )
            )
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
            .andExpect(jsonPath("$.data.products[0].productId").value(2))
            .andExpect(jsonPath("$.data.products[0].name").value("PT 20회"))
            .andExpect(jsonPath("$.data.products[0].totalPrice").value(900000))
            .andExpect(jsonPath("$.data.products[0].trainerName").value("최전문"))
            .andExpect(jsonPath("$.data.products[0].category").value("DIET"))
            .andExpect(jsonPath("$.data.products[0].thumbnailUrl").value("https://example.com/thumb2.jpg"))
            .andExpect(jsonPath("$.data.products[0].likesCount").value(150))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

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
    void getProductReturnsDetail() throws Exception {
        // given
        ProductDetailResponse serviceResult = new ProductDetailResponse(
            1L,
            "PT 10회 집중관리",
            "10회 세션으로 구성된 맞춤형 집중 관리 프로그램입니다.",
            "DIET",
            new BigDecimal("500000"),
            10,
            120L,
            new TrainerInfo(15L, "박전문", "피트니스 센터 강남점"),
            List.of(new ImageInfo("https://example.com/image_main.jpg", 1)),
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
            .andExpect(jsonPath("$.data.name").value("PT 10회 집중관리"))
            .andExpect(jsonPath("$.data.description").value("10회 세션으로 구성된 맞춤형 집중 관리 프로그램입니다."))
            .andExpect(jsonPath("$.data.category").value("DIET"))
            .andExpect(jsonPath("$.data.totalPrice").value(500000))
            .andExpect(jsonPath("$.data.sessionCount").value(10))
            .andExpect(jsonPath("$.data.likesCount").value(120))
            .andExpect(jsonPath("$.data.trainerInfo.trainerId").value(15))
            .andExpect(jsonPath("$.data.trainerInfo.trainerName").value("박전문"))
            .andExpect(jsonPath("$.data.trainerInfo.gymName").value("피트니스 센터 강남점"))
            .andExpect(jsonPath("$.data.images[0].imageUrl").value("https://example.com/image_main.jpg"))
            .andExpect(jsonPath("$.data.images[0].displayOrder").value(1))
            .andExpect(jsonPath("$.data.reviews[0].reviewId").value(1))
            .andExpect(jsonPath("$.data.reviews[0].reviewerName").value("김회원"))
            .andExpect(jsonPath("$.data.reviews[0].rating").value(5))
            .andExpect(jsonPath("$.data.reviews[0].content").value("덕분에 목표 달성했습니다!"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(productService).should().getProductDetail(1L);
    }

    @Test
    @DisplayName("PT 상품 수정 시 갱신된 정보를 반환한다")
    void updateProductReturnsUpdatedSummary() throws Exception {
        // given
        ProductUpdateRequest requestPayload = new ProductUpdateRequest(
            "PT 집중관리 12회",
            "12회 구성으로 업그레이드된 프로그램",
            "STRENGTH",
            new BigDecimal("60000"),
            12,
            "https://example.com/new-thumb.jpg"
        );

        ProductUpdateResponse serviceResult = new ProductUpdateResponse(1L, "PT 집중관리 12회", new BigDecimal("60000"), 12);

        given(productService.updateProduct(any(Long.class), any(ProductUpdateRequest.class)))
            .willReturn(serviceResult);

        // when
        mockMvc.perform(put("/api/products/{productId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.productId").value(1))
            .andExpect(jsonPath("$.data.name").value("PT 집중관리 12회"))
            .andExpect(jsonPath("$.data.pricePerSession").value(60000))
            .andExpect(jsonPath("$.data.sessionCount").value(12))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ProductUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ProductUpdateRequest.class);
        then(productService).should().updateProduct(idCaptor.capture(), requestCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(1L);
        ProductUpdateRequest captured = requestCaptor.getValue();
        assertThat(captured.name()).isEqualTo("PT 집중관리 12회");
        assertThat(captured.description()).isEqualTo("12회 구성으로 업그레이드된 프로그램");
        assertThat(captured.category()).isEqualTo("STRENGTH");
        assertThat(captured.pricePerSession()).isEqualByComparingTo("60000");
        assertThat(captured.sessionCount()).isEqualTo(12);
        assertThat(captured.thumbnailUrl()).isEqualTo("https://example.com/new-thumb.jpg");
    }

    @Test
    @DisplayName("PT 상품 삭제 시 삭제 메시지를 반환한다")
    void deleteProductReturnsMessage() throws Exception {
        // given
        ProductDeleteResponse serviceResult = new ProductDeleteResponse("상품이 삭제되었습니다.");
        given(productService.deleteProduct(1L)).willReturn(serviceResult);

        // when
        mockMvc.perform(delete("/api/products/{productId}", 1L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.message").value("상품이 삭제되었습니다."))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(productService).should().deleteProduct(1L);
    }

    @Test
    @DisplayName("PT 상품 좋아요 토글 시 상태를 반환한다")
    void toggleLikeReturnsStatus() throws Exception {
        // given
        ProductLikeToggleResponse serviceResult = new ProductLikeToggleResponse(true, "좋아요 상태가 변경되었습니다.");
        given(productService.toggleLike(1L)).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/products/{productId}/like", 1L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.liked").value(true))
            .andExpect(jsonPath("$.data.message").value("좋아요 상태가 변경되었습니다."))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(productService).should().toggleLike(1L);
    }
}
