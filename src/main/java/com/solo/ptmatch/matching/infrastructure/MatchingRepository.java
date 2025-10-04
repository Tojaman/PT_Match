package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    List<Matching> findAllByUserId(Long userId);

    @Query("""
        SELECT DISTINCT m FROM Matching m
        JOIN FETCH m.trainerProfile tp
        JOIN FETCH tp.trainer
        JOIN FETCH m.product
        WHERE m.user.id = :userId
    """)
    List<Matching> findAllByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Matching m
        JOIN FETCH m.product
        WHERE m.matchingStatus = :matchingStatus
        AND m.trainerProfile.id = :trainerProfileId
    """)
    List<Matching> findAllByTrainerProfileIdAndStatusWithProduct(@Param("trainerProfileId") Long trainerProfileId, @Param("matchingStatus") MatchingStatus matchingStatus);


    @Query("""
        SELECT m FROM Matching m
        JOIN FETCH m.user
        JOIN FETCH m.product
        JOIN FETCH m.trainerProfile tp
        JOIN FETCH tp.trainer
        LEFT JOIN FETCH m.schedules
        WHERE m.id = :matchingId
    """)
    Optional<Matching> findByIdWithDetails(@Param("matchingId") Long matchingId);

    @Query("""
        SELECT m FROM Matching m
        JOIN FETCH m.trainerProfile tp
        JOIN FETCH tp.trainer
        LEFT JOIN FETCH m.schedules ms
        LEFT JOIN FETCH ms.availableSchedule
        WHERE m.id = :matchingId
    """)
    Optional<Matching> findByIdWithTrainerAndSchedules(@Param("matchingId") Long matchingId);
}