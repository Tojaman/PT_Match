package com.solo.ptmatch.chat.presentation;

import com.solo.ptmatch.chat.application.ChatService;
import com.solo.ptmatch.chat.presentation.request.ChatMessagePublishRequest;
import com.solo.ptmatch.chat.presentation.response.ChatMessageSendResponse;
import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public ChatMessageSendResponse publishMessage(Principal principal, @Valid ChatMessagePublishRequest request) {
        if (principal == null || principal.getName() == null) {
            throw GlobalException.of(ErrorCode.UNAUTHORIZED);
        }
        return chatService.publishChatMessage(principal.getName(), request);
    }
}
