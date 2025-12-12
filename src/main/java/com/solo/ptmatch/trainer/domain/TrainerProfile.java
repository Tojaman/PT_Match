package com.solo.ptmatch.trainer.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "trainer_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerProfile extends BaseEntity {

    private static final BigDecimal DEFAULT_RATING = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false)
    private String bio;

    @Column(name = "career_years", nullable = false)
    private int careerYears;

    @Column(name = "gym_address", nullable = false)
    private String gymAddress;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "trainer_profile_specialties", joinColumns = @JoinColumn(name = "trainer_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false, length = 30)
    private Set<Specialty> specialties = new HashSet<>();

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(name = "average_rating", nullable = false, precision = 4, scale = 2)
    private BigDecimal averageRating;

    private TrainerProfile(
            User user,
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymAddress,
            String profileImageUrl) {
        this.user = user;
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        this.specialties = new HashSet<>(specialties);
        this.gymAddress = gymAddress;
        this.profileImageUrl = profileImageUrl;
        this.averageRating = DEFAULT_RATING;
    }

    public static TrainerProfile create(
            User user,
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymAddress,
            String profileImageUrl) {
        return new TrainerProfile(user, bio, careerYears, specialties, gymAddress, profileImageUrl);
    }

    public void updateProfile(
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymAddress,
            String profileImageUrl) {
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        replaceSpecialties(specialties);
        this.gymAddress = gymAddress;
        this.profileImageUrl = profileImageUrl;
    }

    public void replaceSpecialties(Set<Specialty> specialties) {
        this.specialties.clear();
        this.specialties.addAll(specialties);
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

    public void increaseReviewCount() {
        this.reviewCount += 1;
    }

    public void decreaseReviewCount() {
        if (reviewCount == 0) {
            return;
        }
        this.reviewCount -= 1;
    }

    private void validateCareerYears(int careerYears) {
        if (careerYears < 0) {
            throw new IllegalArgumentException("careerYears must not be negative");
        }
    }
}
