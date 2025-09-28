package com.solo.ptmatch.matching.presentation;

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
import com.solo.ptmatch.matching.application.MatchingService;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingReceivedSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRespondResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
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
@WebMvcTest(MatchingController.class)
class MatchingContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatchingService matchingService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("매칭 신청 시 생성된 매칭 ID와 상태를 반환한다")
    void requestMatchingReturnsCreatedMatching() throws Exception {
        // given
        MatchingRequestCreateRequest requestPayload = new MatchingRequestCreateRequest(1L, "주 2회 PT 받고 싶습니다. 시간 조율 원합니다.");
        MatchingRequestCreateResponse serviceResult = new MatchingRequestCreateResponse(1L, MatchingStatus.PENDING);

        given(matchingService.requestMatching(any(MatchingRequestCreateRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/matching/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.matchingId").value(1))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<MatchingRequestCreateRequest> requestCaptor = ArgumentCaptor.forClass(MatchingRequestCreateRequest.class);
        then(matchingService).should().requestMatching(requestCaptor.capture());

        MatchingRequestCreateRequest captured = requestCaptor.getValue();
        assertThat(captured.productId()).isEqualTo(1L);
        assertThat(captured.message()).isEqualTo("주 2회 PT 받고 싶습니다. 시간 조율 원합니다.");
    }

    @Test
    @DisplayName("보낸 매칭 신청 목록 조회 시 요약 정보를 반환한다")
    void getSentMatchingsReturnsSummaries() throws Exception {
        // given
        List<MatchingSentSummaryResponse> serviceResult = List.of(
            new MatchingSentSummaryResponse(
                1L,
                "PT 10회 집중관리",
                "박전문",
                MatchingStatus.PENDING,
                LocalDateTime.of(2025, 9, 16, 10, 0)
            )
        );

        given(matchingService.getSentMatchings()).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/matching/sent"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].matchingId").value(1))
            .andExpect(jsonPath("$.data[0].productName").value("PT 10회 집중관리"))
            .andExpect(jsonPath("$.data[0].trainerName").value("박전문"))
            .andExpect(jsonPath("$.data[0].status").value("PENDING"))
            .andExpect(jsonPath("$.data[0].createdAt").value("2025-09-16T10:00:00"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(matchingService).should().getSentMatchings();
    }

    @Test
    @DisplayName("받은 매칭 신청 목록 조회 시 신청자 정보를 반환한다")
    void getReceivedMatchingsReturnsSummaries() throws Exception {
        // given
        List<MatchingReceivedSummaryResponse> serviceResult = List.of(
            new MatchingReceivedSummaryResponse(
                1L,
                "PT 10회 집중관리",
                "이운동",
                MatchingStatus.PENDING,
                LocalDateTime.of(2025, 9, 16, 10, 0)
            )
        );

        given(matchingService.getReceivedMatchings()).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/matching/received"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].matchingId").value(1))
            .andExpect(jsonPath("$.data[0].productName").value("PT 10회 집중관리"))
            .andExpect(jsonPath("$.data[0].applicantName").value("이운동"))
            .andExpect(jsonPath("$.data[0].status").value("PENDING"))
            .andExpect(jsonPath("$.data[0].createdAt").value("2025-09-16T10:00:00"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(matchingService).should().getReceivedMatchings();
    }

    @Test
    @DisplayName("매칭 응답 시 처리 메시지와 채팅방 ID를 반환한다")
    void respondMatchingReturnsMessageAndChatRoom() throws Exception {
        // given
        MatchingRespondRequest requestPayload = new MatchingRespondRequest(MatchingStatus.ACCEPTED);
        MatchingRespondResponse serviceResult = new MatchingRespondResponse("요청이 처리되었습니다.", 12L);

        given(matchingService.respondMatching(any(Long.class), any(MatchingRespondRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(put("/api/matching/{matchingId}/respond", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.message").value("요청이 처리되었습니다."))
            .andExpect(jsonPath("$.data.chatRoomId").value(12))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<MatchingRespondRequest> requestCaptor = ArgumentCaptor.forClass(MatchingRespondRequest.class);
        then(matchingService).should().respondMatching(idCaptor.capture(), requestCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(1L);
        MatchingRespondRequest captured = requestCaptor.getValue();
        assertThat(captured.status()).isEqualTo(MatchingStatus.ACCEPTED);
    }
}
