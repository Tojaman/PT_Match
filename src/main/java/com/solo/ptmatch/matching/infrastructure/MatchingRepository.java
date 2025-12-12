package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    @Query("""
        SELECT DISTINCT m FROM Matching m
        JOIN FETCH m.trainerProfile tp
        JOIN FETCH tp.user
        WHERE m.user.id = :userId
    """)
    List<Matching> findAllByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Matching m
        WHERE m.trainerProfile.id = :trainerProfileId
        AND (:status IS NULL OR m.matchingStatus IN :status)
    """)
    Page<Matching> findAllByTrainerProfileIdAndStatus(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("status") List<MatchingStatus> status,
            Pageable pageable // Sort 포함(order by 포함)
    );


    @Query("""
        SELECT m FROM Matching m
        JOIN FETCH m.user
        JOIN FETCH m.trainerProfile tp
        JOIN FETCH tp.user
        JOIN FETCH m.schedules
        WHERE m.id = :matchingId
    """)
    Optional<Matching> findByIdWithDetails(@Param("matchingId") Long matchingId);

    @Query("""
        SELECT m FROM Matching m
        JOIN FETCH m.trainerProfile tp
        JOIN FETCH tp.user
        JOIN FETCH m.schedules ms
        JOIN FETCH ms.availableSchedule
        WHERE m.id = :matchingId
    """)
    Optional<Matching> findByIdWithUserAndSchedules(@Param("matchingId") Long matchingId);

    Optional<Matching> findByIdAndUserId(Long matchingId, Long userId);
}