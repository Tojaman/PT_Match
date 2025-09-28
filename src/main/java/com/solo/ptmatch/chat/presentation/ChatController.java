package com.solo.ptmatch.chat.presentation;

import com.solo.ptmatch.chat.application.ChatService;
import com.solo.ptmatch.chat.presentation.request.ChatMessageSendRequest;
import com.solo.ptmatch.chat.presentation.response.ChatMessageSendResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessagesResponse;
import com.solo.ptmatch.chat.presentation.response.ChatRoomSummaryResponse;
import com.solo.ptmatch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "채팅방 목록", description = "사용자의 채팅방 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공")
    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomSummaryResponse>> getChatRooms() {
        List<ChatRoomSummaryResponse> response = chatService.getChatRooms();
        return ApiResponse.success(response);
    }

    @Operation(summary = "채팅 메시지 조회", description = "특정 채팅방의 메시지를 페이지네이션하여 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅 메시지 조회 성공")
    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<ChatMessagesResponse> getChatMessages(
        @PathVariable Long roomId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        ChatMessagesResponse response = chatService.getChatMessages(roomId, page, size);
        return ApiResponse.success(response);
    }

    @Operation(summary = "채팅 메시지 전송", description = "REST API를 통해 채팅 메시지를 전송한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅 메시지 전송 성공")
    @PostMapping("/rooms/{roomId}/messages")
    public ApiResponse<ChatMessageSendResponse> sendChatMessage(
        @PathVariable Long roomId,
        @Valid @RequestBody ChatMessageSendRequest request
    ) {
        ChatMessageSendResponse response = chatService.sendChatMessage(roomId, request);
        return ApiResponse.success(response);
    }
}
