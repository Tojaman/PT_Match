package com.solo.ptmatch.chat.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "chat_rooms",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_chat_room_pair", columnNames = {"user_a_id", "user_b_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    private ChatRoom(User userA, User userB) {
        this.userA = Objects.requireNonNull(userA, "userA must not be null");
        this.userB = Objects.requireNonNull(userB, "userB must not be null");
        if (Objects.equals(userA.getId(), userB.getId())) {
            throw new IllegalArgumentException("userA and userB must be different");
        }
        if (userA.getId() == null || userB.getId() == null) {
            throw new IllegalArgumentException("participants must be persisted users");
        }
        if (userA.getId() > userB.getId()) {
            throw new IllegalArgumentException("userA.id must be less than userB.id");
        }
    }

    public static ChatRoom create(User userA, User userB) {
        return new ChatRoom(userA, userB);
    }

    public boolean hasParticipant(Long userId) {
        return userA.getId().equals(userId) || userB.getId().equals(userId);
    }

    public User getPartner(Long userId) {
        if (userA.getId().equals(userId)) {
            return userB;
        }
        if (userB.getId().equals(userId)) {
            return userA;
        }
        throw new IllegalArgumentException("user is not a participant of this chat room");
    }
}
