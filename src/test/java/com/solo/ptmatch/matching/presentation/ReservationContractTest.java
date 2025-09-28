package com.solo.ptmatch.matching.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.matching.application.ReservationService;
import com.solo.ptmatch.matching.domain.ReservationStatus;
import com.solo.ptmatch.matching.presentation.request.ReservationCreateRequest;
import com.solo.ptmatch.matching.presentation.response.ReservationCancelResponse;
import com.solo.ptmatch.matching.presentation.response.ReservationDetailResponse;
import com.solo.ptmatch.matching.presentation.response.ReservationSummaryResponse;
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
@WebMvcTest(ReservationController.class)
class ReservationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("예약 생성 시 예약 상세 정보를 반환한다")
    void createReservationReturnsDetail() throws Exception {
        // given
        ReservationCreateRequest requestPayload = new ReservationCreateRequest(1L, 101L);
        ReservationDetailResponse serviceResult = new ReservationDetailResponse(
            201L,
            1L,
            "박전문",
            "김헬스",
            LocalDateTime.of(2025, 9, 22, 10, 0),
            ReservationStatus.PENDING
        );

        given(reservationService.createReservation(any(ReservationCreateRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.reservationId").value(201))
            .andExpect(jsonPath("$.data.matchingId").value(1))
            .andExpect(jsonPath("$.data.trainerName").value("박전문"))
            .andExpect(jsonPath("$.data.userName").value("김헬스"))
            .andExpect(jsonPath("$.data.reservationTime").value("2025-09-22T10:00:00"))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<ReservationCreateRequest> requestCaptor = ArgumentCaptor.forClass(ReservationCreateRequest.class);
        then(reservationService).should().createReservation(requestCaptor.capture());

        ReservationCreateRequest captured = requestCaptor.getValue();
        assertThat(captured.matchingId()).isEqualTo(1L);
        assertThat(captured.scheduleId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("내 예약 목록 조회 시 상태 필터를 전달한다")
    void getMyReservationsReturnsSummaries() throws Exception {
        // given
        List<ReservationSummaryResponse> serviceResult = List.of(
            new ReservationSummaryResponse(
                201L,
                "박전문",
                LocalDateTime.of(2025, 9, 22, 10, 0),
                ReservationStatus.SCHEDULED
            )
        );

        given(reservationService.getMyReservations("SCHEDULED")).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/reservations/me")
                .param("status", "SCHEDULED"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].reservationId").value(201))
            .andExpect(jsonPath("$.data[0].partnerName").value("박전문"))
            .andExpect(jsonPath("$.data[0].reservationTime").value("2025-09-22T10:00:00"))
            .andExpect(jsonPath("$.data[0].status").value("SCHEDULED"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(reservationService).should().getMyReservations("SCHEDULED");
    }

    @Test
    @DisplayName("예약 상세 조회 시 예약 정보를 반환한다")
    void getReservationDetailReturnsDetail() throws Exception {
        // given
        ReservationDetailResponse serviceResult = new ReservationDetailResponse(
            201L,
            1L,
            "박전문",
            "김헬스",
            LocalDateTime.of(2025, 9, 22, 10, 0),
            ReservationStatus.SCHEDULED
        );

        given(reservationService.getReservationDetail(201L)).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/reservations/{reservationId}", 201L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.reservationId").value(201))
            .andExpect(jsonPath("$.data.matchingId").value(1))
            .andExpect(jsonPath("$.data.trainerName").value("박전문"))
            .andExpect(jsonPath("$.data.userName").value("김헬스"))
            .andExpect(jsonPath("$.data.reservationTime").value("2025-09-22T10:00:00"))
            .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(reservationService).should().getReservationDetail(201L);
    }

    @Test
    @DisplayName("예약 취소 시 취소 메시지를 반환한다")
    void cancelReservationReturnsMessage() throws Exception {
        // given
        ReservationCancelResponse serviceResult = new ReservationCancelResponse("예약이 취소되었습니다.");
        given(reservationService.cancelReservation(201L)).willReturn(serviceResult);

        // when
        mockMvc.perform(delete("/api/reservations/{reservationId}", 201L))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.message").value("예약이 취소되었습니다."))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(reservationService).should().cancelReservation(201L);
    }
}
