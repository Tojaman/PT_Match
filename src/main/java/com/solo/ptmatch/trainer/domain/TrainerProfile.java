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

    private String gymName;

    @Column(name = "gym_address", nullable = false)
    private String gymAddress;

    @Column(name = "gym_latitude", nullable = false)
    private double gymLatitude;

    @Column(name = "gym_longitude", nullable = false)
    private double gymLongitude;

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

    @Column(name = "price_per_session")
    private Integer pricePerSession;

    private TrainerProfile(
            User user,
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymName,
            String gymAddress,
            double gymLatitude,
            double gymLongitude,
            String profileImageUrl,
            Integer pricePerSession) {
        this.user = user;
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        this.specialties = new HashSet<>(specialties);
        this.gymAddress = gymAddress;
        this.profileImageUrl = profileImageUrl;
        this.pricePerSession = pricePerSession;
        this.averageRating = DEFAULT_RATING;
    }

    public static TrainerProfile create(
            User user,
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymName,
            String gymAddress,
            double gymLatitude,
            double gymLongitude,
            String profileImageUrl,
            Integer pricePerSession) {
        return new TrainerProfile(user, bio, careerYears, specialties, gymName, gymAddress, gymLatitude, gymLongitude, profileImageUrl, pricePerSession);
    }

    public void updateProfile(
            String bio,
            int careerYears,
            Set<Specialty> specialties,
            String gymName,
            String gymAddress,
            double gymLatitude,
            double gymLongitude,
            String profileImageUrl,
            Integer pricePerSession) {
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        replaceSpecialties(specialties);
        this.gymAddress = gymAddress;
        this.gymName = gymName;
        this.gymLatitude = gymLatitude;
        this.gymLongitude = gymLongitude;
        this.profileImageUrl = profileImageUrl;
        this.pricePerSession = pricePerSession;
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

    public void addReviewRating(int rating) {
        BigDecimal totalRating = this.averageRating.multiply(BigDecimal.valueOf(this.reviewCount))
                .add(BigDecimal.valueOf(rating));
        this.reviewCount++;
        this.averageRating = totalRating.divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);
    }

    public void updateReviewRating(int oldRating, int newRating) {
        BigDecimal totalRating = this.averageRating.multiply(BigDecimal.valueOf(this.reviewCount));
        totalRating = totalRating.subtract(BigDecimal.valueOf(oldRating)).add(BigDecimal.valueOf(newRating));
        this.averageRating = totalRating.divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);
    }

    public void deleteReviewRating(int rating) {
        if (this.reviewCount <= 1) {
            this.reviewCount = 0;
            this.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return;
        }
        BigDecimal totalRating = this.averageRating.multiply(BigDecimal.valueOf(this.reviewCount));
        totalRating = totalRating.subtract(BigDecimal.valueOf(rating));
        this.reviewCount--;
        this.averageRating = totalRating.divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);
    }

    private void validateCareerYears(int careerYears) {
        if (careerYears < 0) {
            throw new IllegalArgumentException("careerYears must not be negative");
        }
    }
}
