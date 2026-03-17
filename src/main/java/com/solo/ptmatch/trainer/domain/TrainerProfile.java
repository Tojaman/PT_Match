package com.solo.ptmatch.trainer.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.common.util.S2Util;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "trainer_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerProfile extends BaseEntity {

    private static final BigDecimal DEFAULT_RATING = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal DEFAULT_POPULARITY_SCORE = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    private static final int POPULARITY_CALCULATION_SCALE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio;

    @Column(name = "career_years", nullable = false)
    private int careerYears;

    private String facilityName;

    @Column(name = "facility_address", nullable = false)
    private String facilityAddress;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    // PostGIS Point (SRID 4326 = WGS84)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Column(name = "district_code", length = 10)
    private String districtCode;

    @Column(name = "s2_cell_id")
    private Long s2CellId;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false, length = 30)
    private SportType sportType;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(name = "average_rating", nullable = false, precision = 4, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "popularity_score", nullable = false, precision = 6, scale = 4)
    private BigDecimal popularityScore;

    @Column(name = "price_per_session")
    private Integer pricePerSession;

    private TrainerProfile(
            User user,
            String bio,
            int careerYears,
            SportType sportType,
            String facilityName,
            String facilityAddress,
            double latitude,
            double longitude,
            String profileImageUrl,
            Integer pricePerSession) {
        this.user = user;
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        this.sportType = sportType;
        this.facilityName = facilityName;
        this.facilityAddress = facilityAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = createPoint(longitude, latitude);
        this.s2CellId = S2Util.calculateS2CellId(latitude, longitude);
        this.profileImageUrl = profileImageUrl;
        this.pricePerSession = pricePerSession;
        this.averageRating = DEFAULT_RATING;
        this.popularityScore = DEFAULT_POPULARITY_SCORE;
    }

    public static TrainerProfile create(
            User user,
            String bio,
            int careerYears,
            SportType sportType,
            String facilityName,
            String facilityAddress,
            double latitude,
            double longitude,
            String profileImageUrl,
            Integer pricePerSession) {
        return new TrainerProfile(user, bio, careerYears, sportType, facilityName, facilityAddress, latitude, longitude,
                profileImageUrl, pricePerSession);
    }

    public void updateProfile(
            String bio,
            int careerYears,
            SportType sportType,
            String facilityName,
            String facilityAddress,
            double latitude,
            double longitude,
            String profileImageUrl,
            Integer pricePerSession) {
        this.bio = bio;
        validateCareerYears(careerYears);
        this.careerYears = careerYears;
        this.sportType = sportType;
        this.facilityAddress = facilityAddress;
        this.facilityName = facilityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = createPoint(longitude, latitude);
        this.s2CellId = S2Util.calculateS2CellId(latitude, longitude);
        this.profileImageUrl = profileImageUrl;
        this.pricePerSession = pricePerSession;
    }

    /**
     * S2 Cell ID 업데이트 (마이그레이션용)
     */
    public void updateS2CellId(Long s2CellId) {
        this.s2CellId = s2CellId;
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

    // Bayesian rating = (v / (v + m)) * R + (m / (v + m)) * C
    // v = reviewCount, m = bayesianM, R = averageRating, C = globalAverageRating
    public void recalculatePopularityScore(BigDecimal globalAverageRating) {
        if (this.reviewCount == 0) {
            this.popularityScore = DEFAULT_POPULARITY_SCORE;
            return;
        }

        BigDecimal reviewCountValue = BigDecimal.valueOf(this.reviewCount);
        BigDecimal bayesianM = BigDecimal.valueOf(TrainerPopularityScorePolicy.BAYESIAN_M);
        BigDecimal denominator = reviewCountValue.add(bayesianM); // v + M
        BigDecimal trainerWeight = reviewCountValue.divide(
                denominator,
                POPULARITY_CALCULATION_SCALE,
                RoundingMode.HALF_UP
        );
        BigDecimal globalWeight = bayesianM.divide(
                denominator,
                POPULARITY_CALCULATION_SCALE,
                RoundingMode.HALF_UP
        );

        this.popularityScore = trainerWeight.multiply(this.averageRating)
                .add(globalWeight.multiply(globalAverageRating))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private void validateCareerYears(int careerYears) {
        if (careerYears < 0) {
            throw new IllegalArgumentException("careerYears must not be negative");
        }
    }

    private static Point createPoint(double longitude, double latitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}
