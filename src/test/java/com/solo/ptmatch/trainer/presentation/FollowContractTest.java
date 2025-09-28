package com.solo.ptmatch.trainer.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.trainer.application.TrainerFollowService;
import com.solo.ptmatch.trainer.presentation.response.FollowedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerFollowToggleResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(FollowController.class)
class FollowContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainerFollowService trainerFollowService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("트레이너 팔로우 토글 시 상태를 반환한다")
    void toggleFollowReturnsStatus() throws Exception {
        // given
        TrainerFollowToggleResponse serviceResult = new TrainerFollowToggleResponse(true, "팔로우 상태가 변경되었습니다.");
        given(trainerFollowService.toggleFollow(any(Long.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/trainers/{trainerId}/follow", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.followed").value(true))
            .andExpect(jsonPath("$.data.message").value("팔로우 상태가 변경되었습니다."))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(trainerFollowService).should().toggleFollow(1L);
    }

    @Test
    @DisplayName("팔로우한 트레이너 목록 조회 시 요약 정보를 반환한다")
    void getFollowedTrainersReturnsSummaries() throws Exception {
        // given
        List<FollowedTrainerSummaryResponse> serviceResult = List.of(
            new FollowedTrainerSummaryResponse(
                1L,
                "박전문",
                "피트니스 센터",
                "https://example.com/profile.jpg"
            )
        );

        given(trainerFollowService.getFollowedTrainers()).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/me/follows/trainers"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].trainerId").value(1))
            .andExpect(jsonPath("$.data[0].name").value("박전문"))
            .andExpect(jsonPath("$.data[0].gymName").value("피트니스 센터"))
            .andExpect(jsonPath("$.data[0].profileImageUrl").value("https://example.com/profile.jpg"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(trainerFollowService).should().getFollowedTrainers();
    }
}
