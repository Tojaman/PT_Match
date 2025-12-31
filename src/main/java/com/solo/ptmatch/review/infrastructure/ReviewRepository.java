package com.solo.ptmatch.review.infrastructure;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

        @Query("""
                SELECT r
                FROM Review r
                JOIN FETCH r.matching m
                JOIN FETCH m.trainerProfile t
                JOIN FETCH t.user
                WHERE m.user.id = :userId
                """)
        Page<Review> findUserReviews(Long userId, Pageable pageable);

        @Query("""
                SELECT r
                FROM Review r
                JOIN r.matching m
                JOIN m.user u
                WHERE m.trainerProfile.id = :trainerId
                """)
        Page<Review> findTrainerReviews(Long trainerId, Pageable pageable);

        boolean existsByMatching(Matching matching);

        Optional<Review> findByIdAndMatchingUserId(Long reviewId, Long userId);

        Optional<Review> findByMatchingId(Long matchingId);

        // 기간 내 리뷰 수
        @Query("""
                SELECT COUNT(r) FROM Review r
                WHERE r.matching.trainerProfile.id = :trainerProfileId
                AND r.createdAt >= :start
                AND r.createdAt < :end
                """)
        int countByTrainerProfileIdAndDateRange(
                        @Param("trainerProfileId") Long trainerProfileId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 기간 내 평균 평점
        @Query("""
                SELECT COALESCE(AVG(r.rating), 0) FROM Review r
                WHERE r.matching.trainerProfile.id = :trainerProfileId
                AND r.createdAt >= :start
                AND r.createdAt < :end
                """)
        Double getAverageRatingByTrainerProfileIdAndDateRange(
                        @Param("trainerProfileId") Long trainerProfileId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 별점 분포 (트레이너 전체)
        @Query("""
                SELECT r.rating, COUNT(r) FROM Review r
                WHERE r.matching.trainerProfile.id = :trainerProfileId
                GROUP BY r.rating
                ORDER BY r.rating DESC
                """)
        List<Object[]> getRatingDistribution(@Param("trainerProfileId") Long trainerProfileId);

        // 최근 리뷰 목록
        @Query("""
                SELECT r FROM Review r
                JOIN FETCH r.matching m
                JOIN FETCH m.user
                WHERE m.trainerProfile.id = :trainerProfileId
                ORDER BY r.createdAt DESC
                """)
        List<Review> findRecentReviewsByTrainerProfileId(
                        @Param("trainerProfileId") Long trainerProfileId,
                        Pageable pageable);
}
