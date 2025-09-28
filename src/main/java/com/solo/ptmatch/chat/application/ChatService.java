package com.solo.ptmatch.chat.application;

import com.solo.ptmatch.chat.presentation.request.ChatMessagePublishRequest;
import com.solo.ptmatch.chat.presentation.request.ChatMessageSendRequest;
import com.solo.ptmatch.chat.presentation.response.ChatMessageSendResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessagesResponse;
import com.solo.ptmatch.chat.presentation.response.ChatRoomSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatService {

    public List<ChatRoomSummaryResponse> getChatRooms() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ChatMessagesResponse getChatMessages(Long roomId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ChatMessageSendResponse sendChatMessage(Long roomId, ChatMessageSendRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ChatMessageSendResponse publishChatMessage(ChatMessagePublishRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
