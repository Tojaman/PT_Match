package com.solo.ptmatch.chat.infrastructure;

import com.solo.ptmatch.chat.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByUserAIdAndUserBId(Long userAId, Long userBId);

    @Query(
        "select r from ChatRoom r " +
            "where (r.userA.id = :userId or r.userB.id = :userId) " +
            "and (:cursorRoomId is null or r.id < :cursorRoomId) " +
            "order by r.id desc"
    )
    List<ChatRoom> findByParticipantWithCursor(
        @Param("userId") Long userId,
        @Param("cursorRoomId") Long cursorRoomId,
        Pageable pageable
    );

    @Query("select r from ChatRoom r join fetch r.userA join fetch r.userB where r.id = :roomId")
    Optional<ChatRoom> findByIdWithUsers(@Param("roomId") Long roomId);
}
