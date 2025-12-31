package com.solo.ptmatch.statistics.infrastructure;

import com.solo.ptmatch.statistics.domain.TrainerMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainerMonthlyStatsRepository extends JpaRepository<TrainerMonthlyStats, Long> {

        // 트레이너의 특정 월 통계 조회
        Optional<TrainerMonthlyStats> findByTrainerProfileIdAndYearAndMonth(Long trainerProfileId, Integer year, Integer month);

        // 트레이너의 최근 6개월 통계 조회 (차트용)
        @Query("""
                SELECT s FROM TrainerMonthlyStats s
                WHERE s.trainerProfile.id = :trainerProfileId
                ORDER BY s.year DESC, s.month DESC
                LIMIT 6
                """)
        List<TrainerMonthlyStats> findRecentMonthsByTrainerProfileId(@Param("trainerProfileId") Long trainerProfileId);
}
