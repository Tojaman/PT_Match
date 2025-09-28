package com.solo.ptmatch.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.chat.application.ChatService;
import com.solo.ptmatch.chat.presentation.request.ChatMessageSendRequest;
import com.solo.ptmatch.chat.presentation.response.ChatMessageResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessageSendResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessagesResponse;
import com.solo.ptmatch.chat.presentation.response.ChatPageInfoResponse;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.matching.application.ReservationService;
import com.solo.ptmatch.matching.domain.ReservationStatus;
import com.solo.ptmatch.matching.presentation.request.ReservationCreateRequest;
import com.solo.ptmatch.matching.presentation.response.ReservationDetailResponse;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
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
class ReservationChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("예약 확정 후 채팅 메시지를 전송하고 조회한다")
    void reservationConfirmationAndChatFlow() throws Exception {
        // given - reservation create
        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(1L, 101L);
        ReservationDetailResponse reservationResponse = new ReservationDetailResponse(
            201L,
            1L,
            "박전문",
            "김헬스",
            LocalDateTime.of(2025, 9, 22, 10, 0),
            ReservationStatus.SCHEDULED
        );

        given(reservationService.createReservation(any(ReservationCreateRequest.class)))
            .willReturn(reservationResponse);

        // when - create reservation
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.reservationId").value(201))
            .andExpect(jsonPath("$.data.trainerName").value("박전문"))
            .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        ArgumentCaptor<ReservationCreateRequest> reservationCaptor =
            ArgumentCaptor.forClass(ReservationCreateRequest.class);
        then(reservationService).should().createReservation(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().matchingId()).isEqualTo(1L);
        assertThat(reservationCaptor.getValue().scheduleId()).isEqualTo(101L);

        // given - send chat message
        ChatMessageSendRequest chatRequest = new ChatMessageSendRequest("안녕하세요, 예약 시간 확인 부탁드립니다.");
        ChatMessageSendResponse chatResponse = new ChatMessageSendResponse(
            301L,
            12L,
            1L,
            chatRequest.message(),
            LocalDateTime.of(2025, 9, 22, 10, 5)
        );

        given(chatService.sendChatMessage(eq(12L), any(ChatMessageSendRequest.class)))
            .willReturn(chatResponse);

        // when - send chat message
        mockMvc.perform(post("/api/chat/rooms/{roomId}/messages", 12L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRequest)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.messageId").value(301))
            .andExpect(jsonPath("$.data.roomId").value(12))
            .andExpect(jsonPath("$.data.message").value("안녕하세요, 예약 시간 확인 부탁드립니다."));

        ArgumentCaptor<ChatMessageSendRequest> chatCaptor = ArgumentCaptor.forClass(ChatMessageSendRequest.class);
        then(chatService).should().sendChatMessage(eq(12L), chatCaptor.capture());
        assertThat(chatCaptor.getValue().message()).contains("예약 시간 확인");

        // given - fetch chat messages
        ChatMessagesResponse messagesResponse = new ChatMessagesResponse(
            List.of(new ChatMessageResponse(
                301L,
                1L,
                "안녕하세요, 예약 시간 확인 부탁드립니다.",
                LocalDateTime.of(2025, 9, 22, 10, 5)
            )),
            new ChatPageInfoResponse(0, 20, 1, 1)
        );

        given(chatService.getChatMessages(12L, 0, 20)).willReturn(messagesResponse);

        // when - get chat messages
        mockMvc.perform(get("/api/chat/rooms/{roomId}/messages", 12L)
                .param("page", "0")
                .param("size", "20"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.messages[0].messageId").value(301))
            .andExpect(jsonPath("$.data.messages[0].message").value("안녕하세요, 예약 시간 확인 부탁드립니다."))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));

        then(chatService).should().getChatMessages(12L, 0, 20);
    }
}
