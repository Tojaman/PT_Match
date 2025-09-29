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
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "chat_messages")
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

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ChatMessage(ChatRoom chatRoom, User sender, String message) {
        this.chatRoom = Objects.requireNonNull(chatRoom, "chatRoom must not be null");
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    public static ChatMessage create(ChatRoom chatRoom, User sender, String message) {
        return new ChatMessage(chatRoom, sender, message);
    }

    public void markAsRead() {
        this.read = true;
    }

    public boolean isSender(User user) {
        return this.sender.equals(Objects.requireNonNull(user, "user must not be null"));
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        read = false;
    }
}
