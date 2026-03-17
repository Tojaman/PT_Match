package com.solo.ptmatch.statistics.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.trainer.domain.SportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "trainer_popularity_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerPopularityStats extends BaseEntity {

    private static final BigDecimal ZERO_SUM = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_AVERAGE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false, length = 30)
    private SportType sportType;

    @Column(name = "total_rating_sum", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRatingSum;

    @Column(name = "total_review_count", nullable = false)
    private long totalReviewCount;

    @Column(name = "global_average_rating", nullable = false, precision = 4, scale = 2)
    private BigDecimal globalAverageRating;

    private TrainerPopularityStats(SportType sportType) {
        this.sportType = sportType;
        this.totalRatingSum = ZERO_SUM;
        this.totalReviewCount = 0L;
        this.globalAverageRating = ZERO_AVERAGE;
    }

    public static TrainerPopularityStats create(SportType sportType) {
        return new TrainerPopularityStats(sportType);
    }
}
