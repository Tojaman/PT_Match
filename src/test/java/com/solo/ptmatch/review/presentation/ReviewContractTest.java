package com.solo.ptmatch.review.presentation;

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
import com.solo.ptmatch.review.application.ReviewService;
import com.solo.ptmatch.review.presentation.request.ReviewCreateRequest;
import com.solo.ptmatch.review.presentation.response.MyReviewSummaryResponse;
import com.solo.ptmatch.review.presentation.response.ReviewCreateResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ReviewController.class)
class ReviewContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("후기 작성 시 생성된 리뷰 ID와 메시지를 반환한다")
    void createReviewReturnsCreatedSummary() throws Exception {
        // given
        ReviewCreateRequest requestPayload = new ReviewCreateRequest(1L, 5, "정말 친절하고 전문적이세요!");
        ReviewCreateResponse serviceResult = new ReviewCreateResponse(1L, "후기가 성공적으로 등록되었습니다.");

        given(reviewService.createReview(any(ReviewCreateRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.reviewId").value(1))
            .andExpect(jsonPath("$.data.message").value("후기가 성공적으로 등록되었습니다."))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<ReviewCreateRequest> requestCaptor = ArgumentCaptor.forClass(ReviewCreateRequest.class);
        then(reviewService).should().createReview(requestCaptor.capture());

        ReviewCreateRequest captured = requestCaptor.getValue();
        assertThat(captured.matchingId()).isEqualTo(1L);
        assertThat(captured.rating()).isEqualTo(5);
        assertThat(captured.content()).isEqualTo("정말 친절하고 전문적이세요!");
    }

    @Test
    @DisplayName("내 후기 목록 조회 시 후기 요약을 반환한다")
    void getMyReviewsReturnsSummaries() throws Exception {
        // given
        List<MyReviewSummaryResponse> serviceResult = List.of(
            new MyReviewSummaryResponse(
                1L,
                "박전문",
                5,
                "정말 친절하고 전문적이세요!",
                LocalDateTime.of(2025, 9, 10, 14, 0)
            )
        );

        given(reviewService.getMyReviews()).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/me/reviews"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].reviewId").value(1))
            .andExpect(jsonPath("$.data[0].trainerName").value("박전문"))
            .andExpect(jsonPath("$.data[0].rating").value(5))
            .andExpect(jsonPath("$.data[0].content").value("정말 친절하고 전문적이세요!"))
            .andExpect(jsonPath("$.data[0].createdAt").value("2025-09-10T14:00:00"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(reviewService).should().getMyReviews();
    }
}
