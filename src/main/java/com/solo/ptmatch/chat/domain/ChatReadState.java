package com.solo.ptmatch.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "chat_read_states")
@IdClass(ChatReadState.ChatReadStateId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReadState {

    @Id
    @Column(name = "chat_room_id")
    private Long roomId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    private ChatReadState(Long roomId, Long userId) {
        this.roomId = Objects.requireNonNull(roomId);
        this.userId = Objects.requireNonNull(userId);
    }

    public static ChatReadState initialize(Long roomId, Long userId) {
        return new ChatReadState(roomId, userId);
    }

    public void updateLastRead(Long lastReadMessageId, LocalDateTime lastReadAt) {
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = lastReadAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ChatReadStateId implements Serializable {
        private Long roomId;
        private Long userId;
    }
}
