package com.solo.ptmatch.chat.presentation;

import com.solo.ptmatch.chat.application.ChatService;
import com.solo.ptmatch.chat.presentation.request.ChatMessagePublishRequest;
import com.solo.ptmatch.chat.presentation.response.ChatMessageSendResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat/messages")
    public ChatMessageSendResponse publishMessage(@Valid ChatMessagePublishRequest request) {
        return chatService.publishChatMessage(request);
    }
}
