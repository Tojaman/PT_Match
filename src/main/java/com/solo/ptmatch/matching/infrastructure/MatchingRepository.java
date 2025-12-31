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
import java.time.LocalDateTime;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    @Query("""
                SELECT DISTINCT m FROM Matching m
                JOIN FETCH m.trainerProfile tp
                JOIN FETCH tp.user
                WHERE m.user.id = :userId
                AND (:status IS NULL OR m.matchingStatus IN :status)
            """)
    Page<Matching> findAllByUserIdWithDetails(
            @Param("userId") Long userId,
            @Param("status") List<MatchingStatus> status,
            Pageable pageable);

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
                AND (m.user.id = :userId OR tp.user.id = :userId)
            """)
    Optional<Matching> findByIdWithDetails(@Param("matchingId") Long matchingId, @Param("userId") Long userId);

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

    // 트레이너의 대기 중인 매칭 요청 수
    @Query("""
                SELECT COUNT(m) FROM Matching m
                WHERE m.trainerProfile.id = :trainerProfileId
                AND m.matchingStatus = 'PENDING'
            """)
    int countPendingRequestsByTrainerProfileId(@Param("trainerProfileId") Long trainerProfileId);

    // 트레이너의 활성 회원 수 (ACCEPTED 상태의 고유 회원 수)
    @Query("""
                SELECT COUNT(DISTINCT m.user.id) FROM Matching m
                WHERE m.trainerProfile.id = :trainerProfileId
                AND m.matchingStatus = 'ACCEPTED'
            """)
    int countActiveMembersByTrainerProfileId(@Param("trainerProfileId") Long trainerProfileId);

    // 잔여 횟수 부족 알림 (3회 이하)
    @Query("""
                SELECT m FROM Matching m
                JOIN FETCH m.user
                WHERE m.trainerProfile.id = :trainerProfileId
                AND m.matchingStatus = 'ACCEPTED'
                AND m.remainingSessions <= 3
                ORDER BY m.remainingSessions ASC
            """)
    List<Matching> findLowSessionAlertsByTrainerProfileId(@Param("trainerProfileId") Long trainerProfileId);

    // 기간 내 신규 회원 수 (ACCEPTED 상태 매칭의 생성일 기준)
    @Query("""
                SELECT COUNT(DISTINCT m.user.id) FROM Matching m
                WHERE m.trainerProfile.id = :trainerProfileId
                AND m.matchingStatus = 'ACCEPTED'
                AND m.createdAt >= :start
                AND m.createdAt < :end
            """)
    int countNewMembersByTrainerProfileIdAndDateRange(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 기간 내 활성 회원 수 (해당 월에 세션이 있는 회원)
    @Query("""
                SELECT COUNT(DISTINCT ms.matching.user.id) FROM MatchingSchedule ms
                WHERE ms.matching.trainerProfile.id = :trainerProfileId
                AND ms.startTime >= :start
                AND ms.startTime < :end
            """)
    int countActiveMembersByTrainerProfileIdAndDateRange(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 신규 회원: 해당 트레이너와 첫 매칭인 회원 수
    @Query("""
                SELECT COUNT(DISTINCT m.user.id) FROM Matching m
                WHERE m.trainerProfile.id = :trainerProfileId
                AND m.matchingStatus IN ('ACCEPTED', 'COMPLETED')
                AND m.createdAt >= :start AND m.createdAt < :end
                AND m.user.id NOT IN (
                    SELECT DISTINCT m2.user.id FROM Matching m2
                    WHERE m2.trainerProfile.id = :trainerProfileId
                    AND m2.matchingStatus IN ('ACCEPTED', 'COMPLETED')
                    AND m2.createdAt < :start
                )
            """)
    int countFirstTimeMembers(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 재등록 회원: 이전에 매칭 이력이 있는 회원의 새 매칭
    @Query("""
                SELECT COUNT(DISTINCT m.user.id) FROM Matching m
                WHERE m.trainerProfile.id = :trainerProfileId
                AND m.matchingStatus IN ('ACCEPTED', 'COMPLETED')
                AND m.createdAt >= :start AND m.createdAt < :end
                AND m.user.id IN (
                    SELECT DISTINCT m2.user.id FROM Matching m2
                    WHERE m2.trainerProfile.id = :trainerProfileId
                    AND m2.createdAt < :start
                )
            """)
    int countReRegisteredMembers(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
