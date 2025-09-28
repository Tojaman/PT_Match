package com.solo.ptmatch.chat.infrastructure;

import com.solo.ptmatch.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
