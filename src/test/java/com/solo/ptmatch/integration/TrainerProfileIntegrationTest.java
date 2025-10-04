package com.solo.ptmatch.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.trainer.application.TrainerProfileService;
import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.presentation.request.TrainerCertificationRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerCertificationResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
class TrainerProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerProfileService trainerProfileService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("트레이너 프로필 등록 후 수정 흐름을 검증한다")
    void registerThenUpdateTrainerProfileFlow() throws Exception {
        // given - register
        TrainerProfileUpsertRequest registerRequest = new TrainerProfileUpsertRequest(
            "10년 경력의 전문 트레이너입니다.",
            10,
            Specialty.DIET,
            "서울 강남구 ...",
            "https://example.com/profile.jpg",
            List.of(new TrainerCertificationRequest(
                "생활체육지도사 1급",
                "문화체육관광부",
                LocalDate.of(2023, 1, 1)
            ))
        );

        TrainerProfileUpsertResponse registerResponse = new TrainerProfileUpsertResponse(
            1L,
            registerRequest.bio(),
            registerRequest.careerYears(),
            registerRequest.specialties(),
            registerRequest.gymAddress(),
            registerRequest.profileImageUrl()
        );

        given(trainerProfileService.registerTrainerProfile(any(), any(TrainerProfileUpsertRequest.class)))
            .willReturn(registerResponse);

        // when - register
        mockMvc.perform(post("/api/trainers/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.profileId").value(1))
                .andExpect(jsonPath("$.data.bio").value("10년 경력의 전문 트레이너입니다."))
                .andExpect(jsonPath("$.data.careerYears").value(10))
                .andExpect(jsonPath("$.data.specialties").value(Specialty.DIET.name()))
                .andExpect(jsonPath("$.data.gymAddress").value("서울 강남구 ..."))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.jpg"));

        ArgumentCaptor<String> registerEmailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TrainerProfileUpsertRequest> registerCaptor =
        ArgumentCaptor.forClass(TrainerProfileUpsertRequest.class);
        then(trainerProfileService).should()
            .registerTrainerProfile(registerEmailCaptor.capture(), registerCaptor.capture());
        assertThat(registerEmailCaptor.getValue()).isNull();
        TrainerProfileUpsertRequest capturedRegister = registerCaptor.getValue();
        assertThat(capturedRegister.bio()).isEqualTo("10년 경력의 전문 트레이너입니다.");
//        assertThat(capturedRegister.specialties()).isEqualTo(Specialty.DIET);

        // given - update
        TrainerProfileUpsertRequest updateRequest = new TrainerProfileUpsertRequest(
            "업데이트된 소개",
            12,
            Specialty.REHABILITATION,
            "서울 서초구 ...",
            "https://example.com/new-profile.jpg",
            List.of(new TrainerCertificationRequest(
                "생활체육지도사 2급",
                "대한체육회",
                LocalDate.of(2024, 5, 1)
            ))
        );

        TrainerProfileUpsertResponse updateResponse = new TrainerProfileUpsertResponse(
            1L,
            updateRequest.bio(),
            updateRequest.careerYears(),
            updateRequest.specialties(),
            updateRequest.gymAddress(),
            updateRequest.profileImageUrl()
        );

        given(trainerProfileService.updateTrainerProfile(any(), any(TrainerProfileUpsertRequest.class)))
            .willReturn(updateResponse);

        // when - update
        mockMvc.perform(put("/api/trainers/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.profileId").value(1))
            .andExpect(jsonPath("$.data.bio").value("업데이트된 소개"))
            .andExpect(jsonPath("$.data.careerYears").value(12))
            .andExpect(jsonPath("$.data.specialties").value(Specialty.REHABILITATION.name()))
            .andExpect(jsonPath("$.data.gymAddress").value("서울 서초구 ..."))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/new-profile.jpg"));

        ArgumentCaptor<String> updateEmailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TrainerProfileUpsertRequest> updateCaptor =
        ArgumentCaptor.forClass(TrainerProfileUpsertRequest.class);
        then(trainerProfileService).should()
            .updateTrainerProfile(updateEmailCaptor.capture(), updateCaptor.capture());
        assertThat(updateEmailCaptor.getValue()).isNull();
        TrainerProfileUpsertRequest capturedUpdate = updateCaptor.getValue();
//        assertThat(capturedUpdate.specialties()).isEqualTo(Specialty.REHABILITATION);

        // given - detail
        TrainerDetailResponse detailResponse = new TrainerDetailResponse(
            1L,
            "박전문",
            "업데이트된 소개",
            updateRequest.specialties().name(),
            12,
            updateRequest.gymAddress(),
            updateRequest.profileImageUrl(),
            120L,
            new BigDecimal("4.8"),
            List.of(new TrainerCertificationResponse(1L, "생활체육지도사 2급", "대한체육회", LocalDate.of(2024, 5, 1)))
        );

        given(trainerProfileService.getTrainerDetail(1L)).willReturn(detailResponse);

        // when - get detail
        mockMvc.perform(get("/api/trainers/{trainerId}", 1L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.trainerId").value(1))
            .andExpect(jsonPath("$.data.name").value("박전문"))
            .andExpect(jsonPath("$.data.bio").value("업데이트된 소개"))
            .andExpect(jsonPath("$.data.specialties").value(Specialty.REHABILITATION.name()))
            .andExpect(jsonPath("$.data.careerYears").value(12))
            .andExpect(jsonPath("$.data.gymAddress").value("서울 서초구 ..."))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/new-profile.jpg"))
            .andExpect(jsonPath("$.data.likesCount").value(120))
            .andExpect(jsonPath("$.data.averageRating").value(4.8))
            .andExpect(jsonPath("$.data.certifications[0].name").value("생활체육지도사 2급"));

        then(trainerProfileService).should().getTrainerDetail(1L);
    }
}
