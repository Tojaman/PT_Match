package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.MatchingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.solo.ptmatch.trainer.presentation.response.MonthlyPerformanceDto;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchingScheduleRepository extends JpaRepository<MatchingSchedule, Long> {

    @Query("""
            SELECT ms FROM MatchingSchedule ms
            JOIN FETCH ms.matching m
            JOIN FETCH m.trainerProfile t
            JOIN FETCH t.user
            WHERE m.user.id = :userId
            ORDER BY ms.startTime ASC
            """)
    List<MatchingSchedule> findAllByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
            SELECT ms FROM MatchingSchedule ms
            JOIN FETCH ms.matching m
            JOIN FETCH m.user
            JOIN FETCH m.trainerProfile t
            JOIN FETCH t.user
            WHERE m.trainerProfile.id = :trainerProfileId AND m.matchingStatus = 'ACCEPTED'
            ORDER BY ms.startTime ASC
            """)
    List<MatchingSchedule> findAllByTrainerProfileIdWithDetails(@Param("trainerProfileId") Long trainerProfileId);

    @Query("""
            SELECT COUNT(ms) FROM MatchingSchedule ms
            WHERE ms.matching.trainerProfile.id = :trainerProfileId
            AND ms.startTime >= :start
            AND ms.startTime < :end
            """)
    int countSessionsByTrainerProfileIdAndDateRange(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT ms FROM MatchingSchedule ms
            JOIN FETCH ms.matching m
            JOIN FETCH m.user
            WHERE m.trainerProfile.id = :trainerProfileId
            AND ms.startTime >= :start
            AND ms.startTime < :end
            ORDER BY ms.startTime ASC
            """)
    List<MatchingSchedule> findByTrainerProfileIdAndDateRange(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT new com.solo.ptmatch.trainer.presentation.response.MonthlyPerformanceDto(
                MONTH(ms.startTime), CAST(COUNT(ms) AS int)
            )
            FROM MatchingSchedule ms
            WHERE ms.matching.trainerProfile.id = :trainerProfileId
            AND ms.matching.matchingStatus IN ('ACCEPTED', 'COMPLETED')
            AND ms.startTime >= :startDate
            AND ms.startTime < :endDate
            GROUP BY MONTH(ms.startTime)
            """)
    List<MonthlyPerformanceDto> countSessionsByMonthForTrainerProfile(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 완료된 세션 수
    @Query("""
            SELECT COUNT(ms) FROM MatchingSchedule ms
            WHERE ms.matching.trainerProfile.id = :trainerProfileId
            AND ms.sessionStatus = 'COMPLETED'
            AND ms.startTime >= :start
            AND ms.startTime < :end
            """)
    int countCompletedSessionsByTrainerProfileIdAndDateRange(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 예약된 세션 수 (SCHEDULED 상태)
    @Query("""
            SELECT COUNT(ms) FROM MatchingSchedule ms
            WHERE ms.matching.trainerProfile.id = :trainerProfileId
            AND ms.sessionStatus = 'SCHEDULED'
            AND ms.startTime >= :start
            AND ms.startTime < :end
            """)
    int countScheduledSessionsByTrainerProfileIdAndDateRange(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 월 매출 계산 (완료된 세션 × 세션당 가격)
    @Query("""
            SELECT COALESCE(SUM(ms.matching.pricePerSession), 0) FROM MatchingSchedule ms
            WHERE ms.matching.trainerProfile.id = :trainerProfileId
            AND ms.sessionStatus = 'COMPLETED'
            AND ms.startTime >= :start
            AND ms.startTime < :end
            """)
    Long calculateMonthlyRevenue(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

}
