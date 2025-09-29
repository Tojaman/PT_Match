package com.solo.ptmatch.trainer.domain;

import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "trainer_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerProfile {

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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "trainer_profile_specialties",
            joinColumns = @JoinColumn(name = "trainer_profile_id")
    )
    @Column(name = "specialty", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Specialty> specialties = new LinkedHashSet<>();

    @Column(name = "gym_address", nullable = false)
    private String gymAddress;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "average_rating", nullable = false, precision = 4, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private TrainerProfile(
            User trainer,
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymAddress,
            String profileImageUrl
    ) {
        this.trainer = Objects.requireNonNull(trainer, "trainer must not be null");
        this.bio = Objects.requireNonNull(bio, "bio must not be null");
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        this.specialties = new LinkedHashSet<>();
        applySpecialties(specialties);
        this.gymAddress = Objects.requireNonNull(gymAddress, "gymAddress must not be null");
        this.profileImageUrl = profileImageUrl;
        this.averageRating = DEFAULT_RATING;
    }

    public static TrainerProfile create(
            User trainer,
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymAddress,
            String profileImageUrl
    ) {
        return new TrainerProfile(trainer, bio, careerYears, specialties, gymAddress, profileImageUrl);
    }

    public void updateProfile(
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymAddress,
            String profileImageUrl
    ) {
        this.bio = Objects.requireNonNull(bio, "bio must not be null");
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        applySpecialties(specialties);
        this.gymAddress = Objects.requireNonNull(gymAddress, "gymAddress must not be null");
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
        BigDecimal sanitizedAverage = Objects.requireNonNull(newAverage, "newAverage must not be null");
        this.averageRating = sanitizedAverage.setScale(2, RoundingMode.HALF_UP);
    }

    public Set<Specialty> getSpecialties() {
        return Collections.unmodifiableSet(specialties);
    }

    private void validateCareerYears(int careerYears) {
        if (careerYears < 0) {
            throw new IllegalArgumentException("careerYears must not be negative");
        }
    }

    private void applySpecialties(Set<Specialty> specialties) {
        Objects.requireNonNull(specialties, "specialties must not be null");
        if (specialties.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("specialties must not contain null");
        }
        this.specialties.clear();
        this.specialties.addAll(specialties);
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        likesCount = 0;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
