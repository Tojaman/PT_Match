package com.solo.ptmatch.chat.presentation;

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
import com.solo.ptmatch.chat.presentation.request.ChatMessagePublishRequest;
import com.solo.ptmatch.chat.presentation.request.ChatMessageSendRequest;
import com.solo.ptmatch.chat.presentation.response.ChatMessageSendResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessagesResponse;
import com.solo.ptmatch.chat.presentation.response.ChatPageInfoResponse;
import com.solo.ptmatch.chat.presentation.response.ChatRoomSummaryResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessageResponse;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import java.time.LocalDateTime;
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
@WebMvcTest({ChatController.class, ChatWebSocketController.class})
class ChatContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatWebSocketController chatWebSocketController;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("채팅방 목록 조회 시 요약 정보를 반환한다")
    void getChatRoomsReturnsSummaries() throws Exception {
        // given
        List<ChatRoomSummaryResponse> serviceResult = List.of(
            new ChatRoomSummaryResponse(
                12L,
                "박전문",
                "네, 안녕하세요!",
                LocalDateTime.of(2025, 9, 16, 11, 0),
                1L
            )
        );

        given(chatService.getChatRooms()).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/chat/rooms"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].roomId").value(12))
            .andExpect(jsonPath("$.data[0].partnerName").value("박전문"))
            .andExpect(jsonPath("$.data[0].lastMessage").value("네, 안녕하세요!"))
            .andExpect(jsonPath("$.data[0].lastMessageAt").value("2025-09-16T11:00:00"))
            .andExpect(jsonPath("$.data[0].unreadCount").value(1))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(chatService).should().getChatRooms();
    }

    @Test
    @DisplayName("채팅 메시지 조회 시 메시지 목록과 페이지 정보를 반환한다")
    void getChatMessagesReturnsPagedMessages() throws Exception {
        // given
        ChatMessagesResponse serviceResult = new ChatMessagesResponse(
            List.of(
                new ChatMessageResponse(101L, 15L, "네, 안녕하세요!", LocalDateTime.of(2025, 9, 16, 11, 0))
            ),
            new ChatPageInfoResponse(1, 20, 101, 6)
        );

        given(chatService.getChatMessages(12L, 1, 20)).willReturn(serviceResult);

        // when
        mockMvc.perform(get("/api/chat/rooms/{roomId}/messages", 12L)
                .param("page", "1")
                .param("size", "20"))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.messages[0].messageId").value(101))
            .andExpect(jsonPath("$.data.messages[0].senderId").value(15))
            .andExpect(jsonPath("$.data.messages[0].message").value("네, 안녕하세요!"))
            .andExpect(jsonPath("$.data.messages[0].sentAt").value("2025-09-16T11:00:00"))
            .andExpect(jsonPath("$.data.pageInfo.page").value(1))
            .andExpect(jsonPath("$.data.pageInfo.size").value(20))
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(101))
            .andExpect(jsonPath("$.data.pageInfo.totalPages").value(6))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(chatService).should().getChatMessages(12L, 1, 20);
    }

    @Test
    @DisplayName("채팅 메시지 전송 시 전송된 메시지 요약을 반환한다")
    void sendChatMessageReturnsSummary() throws Exception {
        // given
        ChatMessageSendRequest requestPayload = new ChatMessageSendRequest("네, 감사합니다!");
        ChatMessageSendResponse serviceResult = new ChatMessageSendResponse(
            201L,
            12L,
            1L,
            "네, 감사합니다!",
            LocalDateTime.of(2025, 9, 16, 11, 5)
        );

        given(chatService.sendChatMessage(eq(12L), any(ChatMessageSendRequest.class))).willReturn(serviceResult);

        // when
        mockMvc.perform(post("/api/chat/rooms/{roomId}/messages", 12L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.messageId").value(201))
            .andExpect(jsonPath("$.data.roomId").value(12))
            .andExpect(jsonPath("$.data.senderId").value(1))
            .andExpect(jsonPath("$.data.message").value("네, 감사합니다!"))
            .andExpect(jsonPath("$.data.sentAt").value("2025-09-16T11:05:00"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        then(chatService).should().sendChatMessage(eq(12L), any(ChatMessageSendRequest.class));
    }

    @Test
    @DisplayName("STOMP 메시지 발행 시 서비스에 전달한다")
    void publishMessageDelegatesToService() {
        // given
        ChatMessagePublishRequest payload = new ChatMessagePublishRequest(12L, 1L, "네, 감사합니다!");
        ChatMessageSendResponse serviceResult = new ChatMessageSendResponse(
            201L,
            12L,
            1L,
            "네, 감사합니다!",
            LocalDateTime.of(2025, 9, 16, 11, 5)
        );

        given(chatService.publishChatMessage(payload)).willReturn(serviceResult);

        // when
        ChatMessageSendResponse actual = chatWebSocketController.publishMessage(payload);

        // then
        assertThat(actual).isEqualTo(serviceResult);
        then(chatService).should().publishChatMessage(payload);
    }
}
