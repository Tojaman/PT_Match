package com.solo.ptmatch.chat.domain;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
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
@Table(name = "chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ChatRoom(User member, TrainerProfile trainerProfile, Matching matching) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.trainerProfile = Objects.requireNonNull(trainerProfile, "trainerProfile must not be null");
        this.matching = Objects.requireNonNull(matching, "matching must not be null");
        validateMatchingConsistency();
    }

    public static ChatRoom open(User member, TrainerProfile trainerProfile, Matching matching) {
        return new ChatRoom(member, trainerProfile, matching);
    }

    public boolean belongsTo(User member, TrainerProfile trainerProfile) {
        Objects.requireNonNull(member, "member must not be null");
        Objects.requireNonNull(trainerProfile, "trainerProfile must not be null");
        return this.member.equals(member) && this.trainerProfile.equals(trainerProfile);
    }

    private void validateMatchingConsistency() {
        if (!matching.getUser().equals(member)) {
            throw new IllegalArgumentException("Matching applicant must match chat member");
        }
        if (!matching.getTrainerProfile().equals(trainerProfile)) {
            throw new IllegalArgumentException("Matching trainer must match chat trainer profile");
        }
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
