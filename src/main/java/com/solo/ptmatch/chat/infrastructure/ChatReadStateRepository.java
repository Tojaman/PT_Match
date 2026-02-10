package com.solo.ptmatch.chat.infrastructure;

import com.solo.ptmatch.chat.domain.ChatReadState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatReadStateRepository extends JpaRepository<ChatReadState, ChatReadState.ChatReadStateId> {

    @Query("select rs from ChatReadState rs where rs.roomId = :roomId and rs.userId = :userId")
    Optional<ChatReadState> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
