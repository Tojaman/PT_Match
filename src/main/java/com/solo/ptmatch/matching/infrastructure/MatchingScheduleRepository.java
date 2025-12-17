package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.MatchingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
                    WHERE m.trainerProfile.id = :trainerProfileId
                    ORDER BY ms.startTime ASC
                    """)
    List<MatchingSchedule> findAllByTrainerProfileIdWithDetails(@Param("trainerProfileId") Long trainerProfileId);

    // 트레이너의 오늘 일정 수
    @Query("""
                    SELECT COUNT(ms) FROM MatchingSchedule ms
                    WHERE ms.matching.trainerProfile.id = :trainerProfileId
                    AND ms.startTime >= :startOfDay
                    AND ms.startTime < :endOfDay
                    """)
    int countTodaySessionsByTrainerProfileId(
                    @Param("trainerProfileId") Long trainerProfileId,
                    @Param("startOfDay") LocalDateTime startOfDay,
                    @Param("endOfDay") LocalDateTime endOfDay);
}
