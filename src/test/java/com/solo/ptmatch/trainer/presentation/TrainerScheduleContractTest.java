package com.solo.ptmatch.trainer.presentation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.trainer.application.TrainerScheduleService;
import com.solo.ptmatch.trainer.presentation.request.TrainerSchedule;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleDeleteRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleListRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequestItem;
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

        given(trainerScheduleService.registerTrainerSchedule(eq(email), eq(requestPayload)))
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

        then(trainerScheduleService).should().registerTrainerSchedule(eq(email), eq(requestPayload));
    }

    @DisplayName("트레이너 스케줄 수정 요청 시 성공 응답을 반환한다")
    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer@test.com")
    void updateSchedule_ReturnsSuccess() throws Exception {
        // given
        String email = "trainer@test.com";
        LocalDateTime start = LocalDateTime.of(2025, 1, 2, 10, 0);
        LocalDateTime end = start.plusHours(1);
        TrainerScheduleUpdateRequest requestPayload = new TrainerScheduleUpdateRequest(
                List.of(new TrainerScheduleUpdateRequestItem(100L, start, end))
        );

        TrainerScheduleListResponse serviceResponse = TrainerScheduleListResponse.from(
                List.of(TrainerSchedule.of(start, end))
        );

        given(trainerScheduleService.updateTrainerSchedule(eq(email), eq(requestPayload)))
                .willReturn(serviceResponse);

        // when & then
        mockMvc.perform(patch("/api/trainers/schedules")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.shedules[0].startTime").value("2025-01-02T10:00:00"))
                .andExpect(jsonPath("$.data.shedules[0].endTime").value("2025-01-02T11:00:00"));

        then(trainerScheduleService).should().updateTrainerSchedule(eq(email), eq(requestPayload));
    }

    @DisplayName("트레이너 스케줄 삭제 요청 시 성공 응답을 반환한다")
    @Test
    @WithMockUser(roles = "TRAINER", username = "trainer@test.com")
    void deleteSchedule_ReturnsSuccess() throws Exception {
        // given
        String email = "trainer@test.com";
        TrainerScheduleDeleteRequest requestPayload = new TrainerScheduleDeleteRequest(List.of(100L, 101L));

        // when & then
        mockMvc.perform(delete("/api/trainers/schedules/schedule")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(204))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(trainerScheduleService).should().deleteTrainerSchedule(eq(email), eq(requestPayload));
    }

    @DisplayName("트레이너 스케줄 조회 요청 시 성공 응답을 반환한다")
    @Test
    void getTrainerSchedules_ReturnsSuccess() throws Exception {
        // given
        Long trainerId = 42L;
        LocalDateTime start = LocalDateTime.of(2025, 1, 5, 9, 0);
        LocalDateTime end = start.plusHours(1);
        TrainerScheduleListResponse serviceResponse = TrainerScheduleListResponse.from(
                List.of(TrainerSchedule.of(start, end))
        );

        given(trainerScheduleService.getTrainerSchedules(trainerId)).willReturn(serviceResponse);

        // when & then
        mockMvc.perform(get("/api/trainers/schedules/{trainerId}", trainerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.shedules[0].startTime").value("2025-01-05T09:00:00"))
                .andExpect(jsonPath("$.data.shedules[0].endTime").value("2025-01-05T10:00:00"));

        then(trainerScheduleService).should().getTrainerSchedules(trainerId);
    }
}
