package com.solo.ptmatch.chat.infrastructure;

import com.solo.ptmatch.chat.domain.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query(
        "select m from ChatMessage m " +
            "where m.chatRoom.id = :roomId " +
            "and (:cursorMessageId is null or m.id < :cursorMessageId) " +
            "order by m.id desc"
    )
    List<ChatMessage> findByRoomWithCursor(
        @Param("roomId") Long roomId,
        @Param("cursorMessageId") Long cursorMessageId,
        Pageable pageable
    );

    Optional<ChatMessage> findTopByChatRoomIdOrderByIdDesc(Long roomId);

    Optional<ChatMessage> findByChatRoomIdAndSenderIdAndClientMessageId(Long roomId, Long senderId, String clientMessageId);

    boolean existsByIdAndChatRoomId(Long id, Long roomId);

    long countByChatRoomIdAndSenderIdNot(Long roomId, Long userId);

    long countByChatRoomIdAndIdGreaterThanAndSenderIdNot(Long roomId, Long lastReadMessageId, Long userId);
}
