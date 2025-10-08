package com.solo.ptmatch.matching.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.matching.application.MatchingService;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.domain.SessionStatus;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.UserInfo;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingScheduleSummary;
import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(value = MatchingController.class, excludeAutoConfiguration = ValidationAutoConfiguration.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatchingService matchingService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("매칭 신청을 요청하면 서비스 결과를 감싼 ApiResponse를 반환한다")
    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void requestMatching_ReturnsApiResponse() throws Exception {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 2, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        MatchingRequestCreateRequest requestPayload = new MatchingRequestCreateRequest(
                1L,
                10L,
                List.of(100L, 101L),
                "주 2회 PT 진행 희망합니다.",
                new UserInfo("변경된이름", "custom@example.com", "010-1234-5678")
        );

        MatchingRequestCreateResponse serviceResponse = MatchingRequestCreateResponse.of(
                2000L,
                MatchingStatus.PENDING,
                List.of(
                        MatchingScheduleSummary.of(3000L, 100L, start, end, SessionStatus.SCHEDULED)
                ),
                MatchingUserInfo.of("변경된이름", "custom@example.com", "010-1234-5678")
        );

        given(matchingService.requestMatching(eq("user@example.com"), any(MatchingRequestCreateRequest.class)))
                .willReturn(serviceResponse);

        // when & then
        mockMvc.perform(post("/api/matching/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.matchingId").value(2000))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.schedules[0].matchingScheduleId").value(3000))
                .andExpect(jsonPath("$.data.schedules[0].availableScheduleId").value(100))
                .andExpect(jsonPath("$.data.schedules[0].startTime").value("2025-02-01T09:00:00"))
                .andExpect(jsonPath("$.data.schedules[0].endTime").value("2025-02-01T10:00:00"))
                .andExpect(jsonPath("$.data.schedules[0].sessionStatus").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.matchingUserInfo.name").value("변경된이름"))
                .andExpect(jsonPath("$.data.matchingUserInfo.email").value("custom@example.com"))
                .andExpect(jsonPath("$.data.matchingUserInfo.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(matchingService).should().requestMatching(eq("user@example.com"), any(MatchingRequestCreateRequest.class));
    }
}
