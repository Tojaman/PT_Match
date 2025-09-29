package com.solo.ptmatch.trainer.domain;

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
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "trainer_follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "trainer_profile_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_follow_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private TrainerFollow(User member, TrainerProfile trainerProfile) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.trainerProfile = Objects.requireNonNull(trainerProfile, "trainerProfile must not be null");
        validateDifferentAccounts(member, trainerProfile);
    }

    public static TrainerFollow create(User member, TrainerProfile trainerProfile) {
        return new TrainerFollow(member, trainerProfile);
    }

    public boolean isSameFollower(User member) {
        return this.member.equals(Objects.requireNonNull(member, "member must not be null"));
    }

    public boolean isSameTrainer(TrainerProfile trainerProfile) {
        return this.trainerProfile.equals(Objects.requireNonNull(trainerProfile, "trainerProfile must not be null"));
    }

    public boolean matches(User member, TrainerProfile trainerProfile) {
        return isSameFollower(member) && isSameTrainer(trainerProfile);
    }

    private void validateDifferentAccounts(User member, TrainerProfile trainerProfile) {
        if (trainerProfile.getTrainer().equals(member)) {
            throw new IllegalArgumentException("trainer cannot follow themselves");
        }
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
