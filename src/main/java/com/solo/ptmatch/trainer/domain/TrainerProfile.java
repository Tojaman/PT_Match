package com.solo.ptmatch.trainer.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "trainer_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerProfile  extends BaseEntity {

    private static final BigDecimal DEFAULT_RATING = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_profile_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @Lob
    @Column(nullable = false)
    private String bio;

    @Column(name = "career_years", nullable = false)
    private int careerYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false)
    private Specialty specialty;

    @Column(name = "gym_address", nullable = false)
    private String gymAddress;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "average_rating", nullable = false, precision = 4, scale = 2)
    private BigDecimal averageRating;

    private TrainerProfile(
            User trainer,
            String bio,
            int careerYears,
            Specialty specialty,
            String gymAddress,
            String profileImageUrl
    ) {
        this.trainer = trainer;
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        this.specialty = specialty;
        this.gymAddress = gymAddress;
        this.profileImageUrl = profileImageUrl;
        this.averageRating = DEFAULT_RATING;
    }

    public static TrainerProfile create(
            User trainer,
            String bio,
            int careerYears,
            Specialty specialty,
            String gymAddress,
            String profileImageUrl
    ) {
        return new TrainerProfile(trainer, bio, careerYears, specialty, gymAddress, profileImageUrl);
    }

    public void updateProfile(
            String bio,
            int careerYears,
            Specialty specialty,
            String gymAddress,
            String profileImageUrl
    ) {
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        this.specialty = Objects.requireNonNull(specialty, "specialty must not be null");
        this.gymAddress = gymAddress;
        this.profileImageUrl = profileImageUrl;
    }

    public void increaseLikes() {
        this.likesCount += 1;
    }

    public void decreaseLikes() {
        if (likesCount == 0) {
            return;
        }
        this.likesCount -= 1;
    }

    public void recalculateAverageRating(BigDecimal newAverage) {
        BigDecimal sanitizedAverage = newAverage;
        this.averageRating = sanitizedAverage.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateCareerYears(int careerYears) {
        if (careerYears < 0) {
            throw new IllegalArgumentException("careerYears must not be negative");
        }
    }
}
