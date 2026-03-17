package com.solo.ptmatch.statistics.infrastructure;

import com.solo.ptmatch.statistics.domain.TrainerPopularityStats;
import com.solo.ptmatch.trainer.domain.SportType;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerPopularityStatsRepository extends JpaRepository<TrainerPopularityStats, SportType> {

    @Modifying
    @Query(value = """
            UPDATE trainer_popularity_stats
            SET total_rating_sum = total_rating_sum + :deltaSum,
                total_review_count = total_review_count + :deltaCount,
                global_average_rating = CASE
                    WHEN total_review_count + :deltaCount = 0 THEN 0
                    ELSE ROUND((total_rating_sum + :deltaSum) / (total_review_count + :deltaCount), 2)
                END,
                updated_at = NOW()
            WHERE sport_type = :sportType
            """, nativeQuery = true)
    int applyReviewDelta(
            @Param("sportType") String sportType,
            @Param("deltaSum") BigDecimal deltaSum,
            @Param("deltaCount") long deltaCount
    );

    @Query("""
            select t.globalAverageRating
            from TrainerPopularityStats t
            where t.sportType = :sportType
            """)
    Optional<BigDecimal> findGlobalAverageRatingBySportType(@Param("sportType") SportType sportType);
}
