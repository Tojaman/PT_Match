package com.solo.ptmatch.chat.presentation;

import com.solo.ptmatch.chat.application.ChatService;
import com.solo.ptmatch.chat.presentation.request.ChatReadUpdateRequest;
import com.solo.ptmatch.chat.presentation.request.DirectRoomCreateRequest;
import com.solo.ptmatch.chat.presentation.response.ChatReadUpdateResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessagesResponse;
import com.solo.ptmatch.chat.presentation.response.ChatRoomsResponse;
import com.solo.ptmatch.chat.presentation.response.DirectRoomCreateResponse;
import com.solo.ptmatch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @Operation(summary = "1:1 채팅방 생성/조회", description = "대상 사용자와의 1:1 채팅방을 생성하거나 기존 방을 반환한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅방 생성/조회 성공")
    @PostMapping("/rooms/direct")
    public ApiResponse<DirectRoomCreateResponse> createOrGetDirectRoom(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody DirectRoomCreateRequest request) {
        DirectRoomCreateResponse response = chatService.createOrGetDirectRoom(email, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "채팅방 목록", description = "사용자의 채팅방 목록을 커서 기반으로 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공")
    @GetMapping("/rooms")
    public ApiResponse<ChatRoomsResponse> getChatRooms(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam(defaultValue = "0") Long cursorRoomId,
            @RequestParam(defaultValue = "20") int size) {
        ChatRoomsResponse response = chatService.getChatRooms(email, cursorRoomId, size);
        return ApiResponse.success(response);
    }

    @Operation(summary = "채팅 메시지 조회", description = "특정 채팅방의 메시지를 커서 기반으로 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅 메시지 조회 성공")
    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<ChatMessagesResponse> getChatMessages(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") Long cursorMessageId,
            @RequestParam(defaultValue = "50") int size) {
        ChatMessagesResponse response = chatService.getChatMessages(email, roomId, cursorMessageId, size);
        return ApiResponse.success(response);
    }

    @Operation(summary = "읽음 상태 갱신", description = "채팅방 읽음 위치를 갱신한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽음 상태 갱신 성공")
    @PatchMapping("/rooms/{roomId}/read")
    public ApiResponse<ChatReadUpdateResponse> updateReadState(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatReadUpdateRequest request) {
        ChatReadUpdateResponse response = chatService.updateReadState(email, roomId, request);
        return ApiResponse.success(response);
    }
}
