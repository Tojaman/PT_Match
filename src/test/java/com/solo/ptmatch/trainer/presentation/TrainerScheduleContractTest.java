package com.solo.ptmatch.trainer.presentation;

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
import com.solo.ptmatch.trainer.application.TrainerScheduleService;
import com.solo.ptmatch.trainer.presentation.request.TrainerSchedule;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleListRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(value = TrainerScheduleController.class, excludeAutoConfiguration = ValidationAutoConfiguration.class)
class TrainerScheduleContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerScheduleService trainerScheduleService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("트레이너 스케줄 등록 요청 시 성공 응답을 반환한다")
    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer@test.com")
    void registerSchedule_ReturnsSuccess() throws Exception {
        // given
        String email = "trainer@test.com";
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        TrainerScheduleListRequest requestPayload = new TrainerScheduleListRequest(
                List.of(TrainerSchedule.of(start, end))
        );

        TrainerScheduleListResponse serviceResponse = TrainerScheduleListResponse.from(
                List.of(TrainerSchedule.of(start, end))
        );

        given(trainerScheduleService.registerTrainerSchedule(eq(email), any(TrainerScheduleListRequest.class)))
                .willReturn(serviceResponse);

        // when & then
        mockMvc.perform(post("/api/trainers/schedules")
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.shedules[0].startTime").value("2025-01-01T09:00:00"))
                .andExpect(jsonPath("$.data.shedules[0].endTime").value("2025-01-01T10:00:00"));

        then(trainerScheduleService).should().registerTrainerSchedule(eq(email), any(TrainerScheduleListRequest.class));
    }
}
