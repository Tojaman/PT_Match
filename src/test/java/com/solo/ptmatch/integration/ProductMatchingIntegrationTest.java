package com.solo.ptmatch.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.matching.application.MatchingService;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.product.application.ProductService;
import com.solo.ptmatch.product.presentation.request.ProductSearchRequest;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse.ImageInfo;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse.ReviewInfo;
import com.solo.ptmatch.product.presentation.response.ProductDetailResponse.TrainerInfo;
import com.solo.ptmatch.product.presentation.response.ProductListResponse;
import com.solo.ptmatch.product.presentation.response.ProductSummaryResponse;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
class ProductMatchingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private MatchingService matchingService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("상품 탐색 이후 매칭 신청 흐름을 검증한다")
    void searchProductThenRequestMatchingFlow() throws Exception {
        // given - product list
        ProductListResponse productListResponse = new ProductListResponse(
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

        given(productService.getProducts(any(ProductSearchRequest.class))).willReturn(productListResponse);

        // when - product list
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
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<ProductSearchRequest> searchCaptor = ArgumentCaptor.forClass(ProductSearchRequest.class);
        then(productService).should().getProducts(searchCaptor.capture());
        ProductSearchRequest capturedSearch = searchCaptor.getValue();
        assertThat(capturedSearch.sort()).isEqualTo("POPULAR");
        assertThat(capturedSearch.category()).isEqualTo("DIET");
        assertThat(capturedSearch.minPrice()).isEqualTo(100000);
        assertThat(capturedSearch.maxPrice()).isEqualTo(500000);
        assertThat(capturedSearch.page()).isEqualTo(0);
        assertThat(capturedSearch.size()).isEqualTo(10);

        // given - product detail
        ProductDetailResponse productDetailResponse = new ProductDetailResponse(
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

        given(productService.getProductDetail(1L)).willReturn(productDetailResponse);

        // when - product detail
        mockMvc.perform(get("/api/products/{productId}", 1L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.productId").value(1))
            .andExpect(jsonPath("$.data.name").value("PT 10회 집중관리"))
            .andExpect(jsonPath("$.data.category").value("DIET"))
            .andExpect(jsonPath("$.data.totalPrice").value(500000))
            .andExpect(jsonPath("$.data.trainerInfo.trainerId").value(15))
            .andExpect(jsonPath("$.data.images[0].imageUrl").value("https://example.com/image_main.jpg"));

        then(productService).should().getProductDetail(1L);

        // given - matching request
        MatchingRequestCreateRequest matchingRequest = new MatchingRequestCreateRequest(
            1L,
            "주 2회 PT 받고 싶습니다. 시간 조율 원합니다."
        );

        MatchingRequestCreateResponse matchingResponse = new MatchingRequestCreateResponse(
            1L,
            MatchingStatus.PENDING
        );

        given(matchingService.requestMatching(any(MatchingRequestCreateRequest.class)))
            .willReturn(matchingResponse);

        // when - matching request
        mockMvc.perform(post("/api/matching/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matchingRequest)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.matchingId").value(1))
            .andExpect(jsonPath("$.data.status").value("PENDING"));

        ArgumentCaptor<MatchingRequestCreateRequest> matchingCaptor =
            ArgumentCaptor.forClass(MatchingRequestCreateRequest.class);
        then(matchingService).should().requestMatching(matchingCaptor.capture());
        MatchingRequestCreateRequest capturedMatching = matchingCaptor.getValue();
        assertThat(capturedMatching.productId()).isEqualTo(1L);
        assertThat(capturedMatching.message()).contains("주 2회 PT");
    }
}
