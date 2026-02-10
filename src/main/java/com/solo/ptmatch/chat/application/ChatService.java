package com.solo.ptmatch.chat.application;

import com.solo.ptmatch.chat.domain.ChatMessage;
import com.solo.ptmatch.chat.domain.ChatReadState;
import com.solo.ptmatch.chat.domain.ChatRoom;
import com.solo.ptmatch.chat.infrastructure.ChatMessageRepository;
import com.solo.ptmatch.chat.infrastructure.ChatReadStateRepository;
import com.solo.ptmatch.chat.infrastructure.ChatRoomRepository;
import com.solo.ptmatch.chat.infrastructure.redis.RedisChatEventPublisher;
import com.solo.ptmatch.chat.presentation.request.ChatMessagePublishRequest;
import com.solo.ptmatch.chat.presentation.request.ChatReadUpdateRequest;
import com.solo.ptmatch.chat.presentation.request.DirectRoomCreateRequest;
import com.solo.ptmatch.chat.presentation.response.ChatEventResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessageSendResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessageResponse;
import com.solo.ptmatch.chat.presentation.response.ChatMessagesResponse;
import com.solo.ptmatch.chat.presentation.response.ChatReadUpdateResponse;
import com.solo.ptmatch.chat.presentation.response.ChatRoomSummaryResponse;
import com.solo.ptmatch.chat.presentation.response.ChatRoomsResponse;
import com.solo.ptmatch.chat.presentation.response.DirectRoomCreateResponse;
import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ChatService {

    private static final int DEFAULT_ROOM_SIZE = 20;
    private static final int DEFAULT_MESSAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStateRepository chatReadStateRepository;
    private final UserRepository userRepository;
    private final RedisChatEventPublisher redisChatEventPublisher;

    @Transactional
    public DirectRoomCreateResponse createOrGetDirectRoom(String email, DirectRoomCreateRequest request) {
        User me = findUserByEmail(email);
        User target = userRepository.findById(request.targetUserId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.CHAT_PARTNER_NOT_FOUND));

        if (me.getId().equals(target.getId())) {
            throw GlobalException.of(ErrorCode.INVALID_REQUEST);
        }

        Long userAId = Math.min(me.getId(), target.getId());
        Long userBId = Math.max(me.getId(), target.getId());

        Optional<ChatRoom> existing = chatRoomRepository.findByUserAIdAndUserBId(userAId, userBId);
        if (existing.isPresent()) {
            return new DirectRoomCreateResponse(existing.get().getId(), false);
        }

        User userA = userAId.equals(me.getId()) ? me : target;
        User userB = userBId.equals(me.getId()) ? me : target;

        try {
            ChatRoom created = chatRoomRepository.save(ChatRoom.create(userA, userB));
            return new DirectRoomCreateResponse(created.getId(), true);
        } catch (DataIntegrityViolationException e) {
            ChatRoom room = chatRoomRepository.findByUserAIdAndUserBId(userAId, userBId)
                    .orElseThrow(() -> e);
            return new DirectRoomCreateResponse(room.getId(), false);
        }
    }

    public ChatRoomsResponse getChatRooms(String email, Long cursorRoomId, int size) {
        User me = findUserByEmail(email);
        int normalizedSize = normalizeSize(size, DEFAULT_ROOM_SIZE);
        Long normalizedCursor = normalizeCursor(cursorRoomId);

        List<ChatRoom> rooms = chatRoomRepository.findByParticipantWithCursor(
                me.getId(),
                normalizedCursor,
                PageRequest.of(0, normalizedSize + 1));

        boolean hasNext = rooms.size() > normalizedSize;
        if (hasNext) {
            rooms = new ArrayList<>(rooms.subList(0, normalizedSize));
        }

        Long nextCursorRoomId = hasNext && !rooms.isEmpty() ? rooms.get(rooms.size() - 1).getId() : null;

        List<ChatRoomSummaryResponse> summaries = rooms.stream()
                .map(room -> toRoomSummary(room, me.getId()))
                .toList();

        return new ChatRoomsResponse(summaries, nextCursorRoomId, hasNext);
    }

    public ChatMessagesResponse getChatMessages(String email, Long roomId, Long cursorMessageId, int size) {
        User me = findUserByEmail(email);
        assertParticipant(roomId, me.getId());

        int normalizedSize = normalizeSize(size, DEFAULT_MESSAGE_SIZE);
        Long normalizedCursor = normalizeCursor(cursorMessageId);

        List<ChatMessage> messagesDesc = chatMessageRepository.findByRoomWithCursor(
                roomId,
                normalizedCursor,
                PageRequest.of(0, normalizedSize + 1));

        boolean hasNext = messagesDesc.size() > normalizedSize;
        if (hasNext) {
            messagesDesc = new ArrayList<>(messagesDesc.subList(0, normalizedSize));
        }

        Long nextCursorMessageId = hasNext && !messagesDesc.isEmpty()
                ? messagesDesc.get(messagesDesc.size() - 1).getId()
                : null;

        List<ChatMessage> messagesAsc = new ArrayList<>(messagesDesc);
        Collections.reverse(messagesAsc);

        List<ChatMessageResponse> responses = messagesAsc.stream()
                .map(message -> new ChatMessageResponse(
                        message.getId(),
                        message.getSender().getId(),
                        message.getContent(),
                        message.getCreatedAt()))
                .toList();

        return new ChatMessagesResponse(responses, nextCursorMessageId, hasNext);
    }

    @Transactional
    public ChatMessageSendResponse publishChatMessage(String email, ChatMessagePublishRequest request) {
        return sendMessage(email, request.roomId(), request.clientMessageId(), request.content());
    }

    @Transactional
    public ChatReadUpdateResponse updateReadState(String email, Long roomId, ChatReadUpdateRequest request) {
        User me = findUserByEmail(email);
        assertParticipant(roomId, me.getId());

        if (!chatMessageRepository.existsByIdAndChatRoomId(request.lastReadMessageId(), roomId)) {
            throw GlobalException.of(ErrorCode.CHAT_INVALID_CURSOR);
        }

        ChatReadState readState = chatReadStateRepository.findByRoomIdAndUserId(roomId, me.getId())
                .orElseGet(() -> ChatReadState.initialize(roomId, me.getId()));

        if (readState.getLastReadMessageId() != null
                && readState.getLastReadMessageId() > request.lastReadMessageId()) {
            return new ChatReadUpdateResponse(
                    roomId,
                    me.getId(),
                    readState.getLastReadMessageId(),
                    readState.getLastReadAt());
        }

        LocalDateTime now = LocalDateTime.now();
        readState.updateLastRead(request.lastReadMessageId(), now);
        chatReadStateRepository.save(readState);

        redisChatEventPublisher.publish(ChatEventResponse.readUpdated(roomId, me.getId(), request.lastReadMessageId(), now));

        return new ChatReadUpdateResponse(roomId, me.getId(), request.lastReadMessageId(), now);
    }

    @Transactional
    public ChatMessageSendResponse sendMessage(String email, Long roomId, String clientMessageId, String content) {
        if (!StringUtils.hasText(content)) {
            throw GlobalException.of(ErrorCode.INVALID_REQUEST);
        }
        if (content.length() > 1000) {
            throw GlobalException.of(ErrorCode.CHAT_MESSAGE_TOO_LONG);
        }

        User sender = findUserByEmail(email);
        ChatRoom room = findRoomForParticipant(roomId, sender.getId());

        String normalizedClientMessageId = normalizeClientMessageId(clientMessageId);

        if (normalizedClientMessageId != null) {
            Optional<ChatMessage> existing = chatMessageRepository.findByChatRoomIdAndSenderIdAndClientMessageId(
                    roomId,
                    sender.getId(),
                    normalizedClientMessageId);
            if (existing.isPresent()) {
                return toSendResponse(existing.get());
            }
        }

        ChatMessage savedMessage = chatMessageRepository.save(
                ChatMessage.create(room, sender, normalizedClientMessageId, content));

        redisChatEventPublisher.publish(
                ChatEventResponse.messageCreated(
                        roomId,
                        savedMessage.getId(),
                        sender.getId(),
                        savedMessage.getContent(),
                        savedMessage.getCreatedAt()));

        return toSendResponse(savedMessage);
    }

    private ChatRoomSummaryResponse toRoomSummary(ChatRoom room, Long meId) {
        User partner = room.getPartner(meId);
        Optional<ChatMessage> lastMessageOpt = chatMessageRepository.findTopByChatRoomIdOrderByIdDesc(room.getId());
        Long unreadCount = calculateUnreadCount(room.getId(), meId);

        return new ChatRoomSummaryResponse(
                room.getId(),
                partner.getId(),
                partner.getName(),
                lastMessageOpt.map(ChatMessage::getContent).orElse(null),
                lastMessageOpt.map(ChatMessage::getCreatedAt).orElse(null),
                unreadCount);
    }

    private Long calculateUnreadCount(Long roomId, Long meId) {
        Optional<ChatReadState> readState = chatReadStateRepository.findByRoomIdAndUserId(roomId, meId);
        if (readState.isEmpty() || readState.get().getLastReadMessageId() == null) {
            return chatMessageRepository.countByChatRoomIdAndSenderIdNot(roomId, meId);
        }
        return chatMessageRepository.countByChatRoomIdAndIdGreaterThanAndSenderIdNot(
                roomId,
                readState.get().getLastReadMessageId(),
                meId);
    }

    private ChatMessageSendResponse toSendResponse(ChatMessage message) {
        return new ChatMessageSendResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoom findRoomForParticipant(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.CHAT_ROOM_NOT_FOUND));
        if (!room.hasParticipant(userId)) {
            throw GlobalException.of(ErrorCode.CHAT_FORBIDDEN);
        }
        return room;
    }

    private void assertParticipant(Long roomId, Long userId) {
        findRoomForParticipant(roomId, userId);
    }

    private int normalizeSize(int size, int defaultSize) {
        int resolved = size <= 0 ? defaultSize : size;
        return Math.min(resolved, MAX_PAGE_SIZE);
    }

    private Long normalizeCursor(Long cursor) {
        if (cursor == null || cursor <= 0) {
            return null;
        }
        return cursor;
    }

    private String normalizeClientMessageId(String clientMessageId) {
        if (!StringUtils.hasText(clientMessageId)) {
            return null;
        }
        return clientMessageId.trim();
    }
}
