package com.solo.ptmatch.trainer.presentation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

//// Spring Security 필터를 활성화하여 @AuthenticationPrincipal 등 보안 관련 기능이 테스트에서 정상 동작하도록 설정
@AutoConfigureMockMvc
@WebMvcTest(TrainerFollowController.class)
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

        // toggleFollow() 메소드에 인자로 1L과 아무 String이 들어오면 serviceResult를 반환하도록 설정
        given(trainerFollowService.toggleFollow(eq(1L), anyString())).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/trainers/{trainerId}/follow", 1L)
                .with(user("member@test.com")) // 시큐리티 컨텍스트 사용자 설정
                .with(csrf()) // CSRF 토큰 추가
                .contentType(MediaType.APPLICATION_JSON))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.followed").value(true))
            .andExpect(jsonPath("$.data.message").value("팔로우 상태가 변경되었습니다."))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        // ArgumentCaptor: 컨트롤러가 서비스를 호출할 때 전달한 인자를 캡처(포착)하여 검증하기 위한 도구
        // 1. String 타입의 인자를 캡처할 수 있는 ArgumentCaptor를 생성한다.
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);

        // 2. trainerFollowService의 toggleFollow 메소드가 호출되었는지 확인하되,
        //    두 번째 인자(email)를 emailCaptor로 캡처한다. (첫 번째 인자는 1L인지 eq로 확인)
        then(trainerFollowService).should().toggleFollow(eq(1L), emailCaptor.capture());

        // 3. 캡처된 값(실제로 메소드에 전달된 이메일)이 기대했던 "member@test.com"과 일치하는지 확인한다.
        assertThat(emailCaptor.getValue()).isEqualTo("member@test.com");
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

        // getFollowedTrainers() 메소드에 인자로 아무 String이 들어오면 serviceResult를 반환하도록 설정
        given(trainerFollowService.getFollowedTrainers(eq("member@test.com"))).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/me/follows/trainers")
                .with(user("member@test.com"))) // 시큐리티 컨텍스트 사용자 설정
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].trainerId").value(1L))
            .andExpect(jsonPath("$.data[0].name").value("박전문"))
            .andExpect(jsonPath("$.data[0].gymAddress").value("피트니스 센터"))
            .andExpect(jsonPath("$.data[0].profileImageUrl").value("https://example.com/profile.jpg"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        // ArgumentCaptor: 컨트롤러가 서비스를 호출할 때 전달한 인자를 캡처(포착)하여 검증하기 위한 도구
        // 1. String 타입의 인자를 캡처할 수 있는 ArgumentCaptor를 생성한다.
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);

        // 2. trainerFollowService의 getFollowedTrainers 메소드가 호출되었는지 확인하되,
        //    메소드에 전달된 실제 인자를 emailCaptor로 캡처한다.
        then(trainerFollowService).should().getFollowedTrainers(emailCaptor.capture());

        // 3. 캡처된 값(실제로 메소드에 전달된 이메일)이 기대했던 "member@test.com"과 일치하는지 확인한다.
        assertThat(emailCaptor.getValue()).isEqualTo("member@test.com");
    }
}
