package com.solo.ptmatch.trainer.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.trainer.application.TrainerProfileService;
import com.solo.ptmatch.trainer.presentation.request.TrainerCertificationRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerCertificationResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerReviewResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@WebMvcTest(TrainerController.class)
class TrainerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired // Spring이 제공하는 ObjectMapper를 사용
    private ObjectMapper objectMapper;

    @MockitoBean // @WebMvcTest에서는 @Mock 대신 @MockitoBean 사용
    private TrainerProfileService trainerProfileService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // 기존 setUp() 메서드와 @InjectMocks private TrainerController trainerController;는 @WebMvcTest에서 필요 없으므로 제거됨
    
    @Test
    @DisplayName("트레이너 목록 조회 시 필터 파라미터를 적용해 응답한다")
    void getTrainersReturnsFilteredList() throws Exception {
        // given
        List<TrainerSummaryResponse> serviceResult = List.of(
            new TrainerSummaryResponse(
                1L,
                "박전문",
                String.join(", ", List.of("다이어트", "근력강화")),
                5,
                new BigDecimal("4.8"),
                "피트니스 센터",
                "https://example.com/profile.jpg",
                50,
                120L
            )
        );

        given(trainerProfileService.getTrainerSummaries(any(TrainerSearchRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/trainers")
                .param("specialty", "다이어트")
                .param("region", "서울")
                .param("sort", "rating_desc")
                .param("page", "0")
                .param("size", "10"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data[0].trainerId").value(1))
            .andExpect(jsonPath("$.data[0].name").value("박전문"))
            .andExpect(jsonPath("$.data[0].specialties").value("다이어트, 근력강화"))
            .andExpect(jsonPath("$.data[0].careerYears").value(5))
            .andExpect(jsonPath("$.data[0].averageRating").value(4.8))
            .andExpect(jsonPath("$.data[0].gymAddress").value("피트니스 센터"))
            .andExpect(jsonPath("$.data[0].profileImageUrl").value("https://example.com/profile.jpg"))
            .andExpect(jsonPath("$.data[0].followerCount").value(50))
            .andExpect(jsonPath("$.data[0].reviewCount").value(120))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<TrainerSearchRequest> queryCaptor = ArgumentCaptor.forClass(TrainerSearchRequest.class);
        then(trainerProfileService).should().getTrainerSummaries(queryCaptor.capture());

        TrainerSearchRequest captured = queryCaptor.getValue();
        assertThat(captured.specialty()).isEqualTo("다이어트");
        assertThat(captured.region()).isEqualTo("서울");
        assertThat(captured.sort()).isEqualTo("rating_desc");
        assertThat(captured.page()).isEqualTo(0);
        assertThat(captured.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("트레이너 상세 조회 시 상세 정보를 반환한다")
    void getTrainerDetailReturnsProfile() throws Exception {
        // given
        TrainerDetailResponse serviceResult = new TrainerDetailResponse(
            1L,
            "박전문",
            "10년 경력의 전문 트레이너입니다...",
            String.join(", ", List.of("다이어트", "근력강화")),
            10,
            "서울 강남구 ...",
            "https://example.com/profile.jpg",
            50L,
            new BigDecimal("4.8"),
            List.of(new TrainerScheduleResponse("MON", "09:00", "18:00")),
            List.of(new TrainerReviewResponse(1L, "김회원", 5, "덕분에 목표 달성했습니다!")),
            List.of(new TrainerCertificationResponse(1L, "생활체육지도사 1급", "대한체육회", LocalDate.of(2023, 1, 1)))
        );

        given(trainerProfileService.getTrainerDetail(1L)).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/trainers/{trainerId}", 1L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.trainerId").value(1))
            .andExpect(jsonPath("$.data.name").value("박전문"))
            .andExpect(jsonPath("$.data.bio").value("10년 경력의 전문 트레이너입니다..."))
            .andExpect(jsonPath("$.data.specialties").value("다이어트, 근력강화"))
            .andExpect(jsonPath("$.data.careerYears").value(10))
            .andExpect(jsonPath("$.data.gymAddress").value("서울 강남구 ..."))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.jpg"))
            .andExpect(jsonPath("$.data.likesCount").value(50))
            .andExpect(jsonPath("$.data.averageRating").value(4.8))
            .andExpect(jsonPath("$.data.schedules[0].day").value("MON"))
            .andExpect(jsonPath("$.data.schedules[0].startTime").value("09:00"))
            .andExpect(jsonPath("$.data.schedules[0].endTime").value("18:00"))
            .andExpect(jsonPath("$.data.reviews[0].reviewId").value(1))
            .andExpect(jsonPath("$.data.reviews[0].reviewerName").value("김회원"))
            .andExpect(jsonPath("$.data.reviews[0].rating").value(5))
            .andExpect(jsonPath("$.data.reviews[0].content").value("덕분에 목표 달성했습니다!"))
            .andExpect(jsonPath("$.data.certifications[0].certificationId").value(1))
            .andExpect(jsonPath("$.data.certifications[0].name").value("생활체육지도사 1급"))
            .andExpect(jsonPath("$.data.certifications[0].issuingOrganization").value("대한체육회"))
            .andExpect(jsonPath("$.data.certifications[0].acquisitionDate").value("2023-01-01"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(trainerProfileService).should().getTrainerDetail(1L);
    }

    @Test
    @DisplayName("트레이너 프로필 등록 시 생성된 요약 정보를 반환한다")
    void registerTrainerProfileReturnsSummary() throws Exception {
        // given
        TrainerProfileUpsertRequest requestPayload = new TrainerProfileUpsertRequest(
            "10년 경력의 전문 트레이너입니다.",
            10,
            List.of("다이어트", "근력강화"),
            "서울 강남구 ...",
            "https://example.com/profile.jpg",
            List.of(new TrainerCertificationRequest(
                "생활체육지도사 1급",
                "문화체육관광부",
                LocalDate.of(2023, 1, 1)
            ))
        );

        TrainerProfileUpsertResponse serviceResult = new TrainerProfileUpsertResponse(
            1L,
            "10년 경력의 전문 트레이너입니다.",
            10,
            List.of("다이어트", "근력강화"),
            "서울 강남구 ...",
            "https://example.com/profile.jpg"
        );

        given(trainerProfileService.registerTrainerProfile(any(TrainerProfileUpsertRequest.class)))
            .willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/trainers/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.profileId").value(1))
            .andExpect(jsonPath("$.data.bio").value("10년 경력의 전문 트레이너입니다."))
            .andExpect(jsonPath("$.data.careerYears").value(10))
            .andExpect(jsonPath("$.data.specialties[0]").value("다이어트"))
            .andExpect(jsonPath("$.data.specialties[1]").value("근력강화"))
            .andExpect(jsonPath("$.data.gymAddress").value("서울 강남구 ..."))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.jpg"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<TrainerProfileUpsertRequest> commandCaptor = ArgumentCaptor.forClass(TrainerProfileUpsertRequest.class);
        then(trainerProfileService).should().registerTrainerProfile(commandCaptor.capture());

        TrainerProfileUpsertRequest captured = commandCaptor.getValue();
        assertThat(captured.bio()).isEqualTo("10년 경력의 전문 트레이너입니다.");
        assertThat(captured.careerYears()).isEqualTo(10);
        assertThat(captured.specialties()).containsExactly("다이어트", "근력강화");
        assertThat(captured.gymAddress()).isEqualTo("서울 강남구 ...");
        assertThat(captured.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        assertThat(captured.certifications()).hasSize(1);

        TrainerCertificationRequest capturedCertification = captured.certifications().get(0);
        assertThat(capturedCertification.name()).isEqualTo("생활체육지도사 1급");
        assertThat(capturedCertification.issuingOrganization()).isEqualTo("문화체육관광부");
        assertThat(capturedCertification.acquisitionDate()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    @DisplayName("트레이너 프로필 수정 시 갱신된 요약 정보를 반환한다")
    void updateTrainerProfileReturnsSummary() throws Exception {
        // given
        TrainerProfileUpsertRequest requestPayload = new TrainerProfileUpsertRequest(
            "업데이트된 소개",
            12,
            List.of("재활", "다이어트"),
            "서울 서초구 ...",
            "https://example.com/new-profile.jpg",
            List.of(new TrainerCertificationRequest(
                "생활체육지도사 2급",
                "대한체육회",
                LocalDate.of(2024, 5, 1)
            ))
        );

        TrainerProfileUpsertResponse serviceResult = new TrainerProfileUpsertResponse(
            1L,
            "업데이트된 소개",
            12,
            List.of("재활", "다이어트"),
            "서울 서초구 ...",
            "https://example.com/new-profile.jpg"
        );

        given(trainerProfileService.updateTrainerProfile(any(TrainerProfileUpsertRequest.class)))
            .willReturn(serviceResult);

        // when
        mockMvc.perform(put("/api/trainers/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.profileId").value(1))
            .andExpect(jsonPath("$.data.bio").value("업데이트된 소개"))
            .andExpect(jsonPath("$.data.careerYears").value(12))
            .andExpect(jsonPath("$.data.specialties[0]").value("재활"))
            .andExpect(jsonPath("$.data.specialties[1]").value("다이어트"))
            .andExpect(jsonPath("$.data.gymAddress").value("서울 서초구 ..."))
            .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/new-profile.jpg"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<TrainerProfileUpsertRequest> commandCaptor = ArgumentCaptor.forClass(TrainerProfileUpsertRequest.class);
        then(trainerProfileService).should().updateTrainerProfile(commandCaptor.capture());

        TrainerProfileUpsertRequest captured = commandCaptor.getValue();
        assertThat(captured.bio()).isEqualTo("업데이트된 소개");
        assertThat(captured.careerYears()).isEqualTo(12);
        assertThat(captured.specialties()).containsExactly("재활", "다이어트");
        assertThat(captured.gymAddress()).isEqualTo("서울 서초구 ...");
        assertThat(captured.profileImageUrl()).isEqualTo("https://example.com/new-profile.jpg");
        assertThat(captured.certifications()).hasSize(1);

        TrainerCertificationRequest capturedCertification = captured.certifications().get(0);
        assertThat(capturedCertification.name()).isEqualTo("생활체육지도사 2급");
        assertThat(capturedCertification.issuingOrganization()).isEqualTo("대한체육회");
        assertThat(capturedCertification.acquisitionDate()).isEqualTo(LocalDate.of(2024, 5, 1));
    }
}
