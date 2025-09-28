package com.solo.ptmatch.chat.infrastructure;

import com.solo.ptmatch.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
