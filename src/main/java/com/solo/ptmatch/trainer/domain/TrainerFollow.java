package com.solo.ptmatch.trainer.domain;

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
public class TrainerFollow  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_follow_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    private TrainerFollow(User user, TrainerProfile trainerProfile) {
        this.user = user;
        this.trainerProfile = trainerProfile;
        validateDifferentAccounts(user, trainerProfile);
    }

    public static TrainerFollow create(User user, TrainerProfile trainerProfile) {
        return new TrainerFollow(user, trainerProfile);
    }

    public boolean isSameFollower(User user) {
        return this.user.equals(user);
    }

    public boolean isSameTrainer(TrainerProfile trainerProfile) {
        return this.trainerProfile.equals(trainerProfile);
    }

    public boolean matches(User user, TrainerProfile trainerProfile) {
        return isSameFollower(user) && isSameTrainer(trainerProfile);
    }

    private void validateDifferentAccounts(User user, TrainerProfile trainerProfile) {
        if (trainerProfile.getUser().equals(user)) {
            throw new IllegalArgumentException("trainer cannot follow themselves");
        }
    }
}
