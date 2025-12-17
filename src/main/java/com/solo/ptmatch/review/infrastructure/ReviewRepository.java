package com.solo.ptmatch.review.infrastructure;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
