package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.MatchingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
