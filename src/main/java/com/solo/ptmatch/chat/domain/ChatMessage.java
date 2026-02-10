package com.solo.ptmatch.chat.domain;

import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "chat_messages",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_chat_message_client",
            columnNames = {"chat_room_id", "sender_id", "client_message_id"}
        )
    },
    indexes = {
        @Index(name = "idx_message_room_id_id", columnList = "chat_room_id,chat_message_id"),
        @Index(name = "idx_message_sender_created", columnList = "sender_id,created_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "client_message_id", length = 64)
    private String clientMessageId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ChatMessage(ChatRoom chatRoom, User sender, String clientMessageId, String content) {
        this.chatRoom = Objects.requireNonNull(chatRoom, "chatRoom must not be null");
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
        this.clientMessageId = clientMessageId;
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    public static ChatMessage create(ChatRoom chatRoom, User sender, String clientMessageId, String content) {
        return new ChatMessage(chatRoom, sender, clientMessageId, content);
    }

    public boolean isSender(User user) {
        return this.sender.equals(Objects.requireNonNull(user, "user must not be null"));
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
