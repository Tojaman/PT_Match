package com.solo.ptmatch.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.matching.application.MatchingService;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingRespondResponse;
import com.solo.ptmatch.product.application.ProductService;
import com.solo.ptmatch.product.presentation.response.ProductLikeToggleResponse;
import com.solo.ptmatch.review.application.ReviewService;
import com.solo.ptmatch.review.presentation.request.ReviewCreateRequest;
import com.solo.ptmatch.review.presentation.response.ReviewCreateResponse;
import com.solo.ptmatch.trainer.application.TrainerFollowService;
import com.solo.ptmatch.trainer.presentation.response.TrainerFollowToggleResponse;
import com.solo.ptmatch.user.infrastructure.UserRepository;
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
class InteractionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchingService matchingService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private TrainerFollowService trainerFollowService;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("매칭 확정 이후 리뷰 작성과 팔로우·좋아요 흐름을 검증한다")
    void postMatchingInteractionFlow() throws Exception {
        // given - matching respond
        MatchingRespondRequest respondRequest = new MatchingRespondRequest(MatchingStatus.ACCEPTED);
        MatchingRespondResponse respondResponse = new MatchingRespondResponse("요청이 처리되었습니다.", 12L);

        given(matchingService.respondMatching(eq(1L), any(MatchingRespondRequest.class)))
            .willReturn(respondResponse);

        // when - respond matching
        mockMvc.perform(put("/api/matching/{matchingId}/respond", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(respondRequest)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.message").value("요청이 처리되었습니다."))
            .andExpect(jsonPath("$.data.chatRoomId").value(12));

        ArgumentCaptor<MatchingRespondRequest> respondCaptor = ArgumentCaptor.forClass(MatchingRespondRequest.class);
        then(matchingService).should().respondMatching(eq(1L), respondCaptor.capture());
        assertThat(respondCaptor.getValue().status()).isEqualTo(MatchingStatus.ACCEPTED);

        // given - create review
        ReviewCreateRequest reviewRequest = new ReviewCreateRequest(1L, 5, "정말 친절하고 전문적이세요!");
        ReviewCreateResponse reviewResponse = new ReviewCreateResponse(1L, "후기가 성공적으로 등록되었습니다.");

        given(reviewService.createReview(any(ReviewCreateRequest.class))).willReturn(reviewResponse);

        // when - create review
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.reviewId").value(1))
            .andExpect(jsonPath("$.data.message").value("후기가 성공적으로 등록되었습니다."));

        ArgumentCaptor<ReviewCreateRequest> reviewCaptor = ArgumentCaptor.forClass(ReviewCreateRequest.class);
        then(reviewService).should().createReview(reviewCaptor.capture());
        ReviewCreateRequest capturedReview = reviewCaptor.getValue();
        assertThat(capturedReview.matchingId()).isEqualTo(1L);
        assertThat(capturedReview.rating()).isEqualTo(5);

        // given - follow trainer
        TrainerFollowToggleResponse followResponse = new TrainerFollowToggleResponse(true, "팔로우 상태가 변경되었습니다.");
        given(trainerFollowService.toggleFollow(15L)).willReturn(followResponse);

        // when - toggle follow
        mockMvc.perform(post("/api/trainers/{trainerId}/follow", 15L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.followed").value(true))
            .andExpect(jsonPath("$.data.message").value("팔로우 상태가 변경되었습니다."));

        then(trainerFollowService).should().toggleFollow(15L);

        // given - like product
        ProductLikeToggleResponse likeResponse = new ProductLikeToggleResponse(true, "좋아요 상태가 변경되었습니다.");
        given(productService.toggleLike(5L)).willReturn(likeResponse);

        // when - toggle like
        mockMvc.perform(post("/api/products/{productId}/like", 5L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.liked").value(true))
            .andExpect(jsonPath("$.data.message").value("좋아요 상태가 변경되었습니다."));

        then(productService).should().toggleLike(5L);
    }
}
