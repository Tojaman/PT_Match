package com.solo.ptmatch.review.infrastructure;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.review.domain.Review;
import com.solo.ptmatch.review.presentation.response.MyReviewSummaryResponse;
import com.solo.ptmatch.review.presentation.response.ProductReviewSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
            SELECT new com.solo.ptmatch.review.presentation.response.MyReviewSummaryResponse(
                r.id,
                r.rating,
                r.content,
                p.title,
                r.createdAt
            )
            FROM Review r
            JOIN r.matching m
            JOIN m.product p
            WHERE m.user.id = :userId
            ORDER BY r.createdAt DESC
            """)
    Page<MyReviewSummaryResponse> findUserReviews(Long userId, Pageable pageable);

    @Query("""
            SELECT new com.solo.ptmatch.review.presentation.response.ProductReviewSummaryResponse(
                r.id,
                u.name,
                r.rating,
                r.content,
                r.createdAt
                )
            FROM Review r
            JOIN r.matching m
            JOIN m.user u
            WHERE m.product.id = :productId
            ORDER BY r.createdAt DESC
            """)
    Page<ProductReviewSummaryResponse> findProductReview(Long productId, Pageable pageable);

    @Query("""
            SELECT new com.solo.ptmatch.review.presentation.response.TrainerReviewSummaryResponse(
                r.id,
                u.name,
                r.rating,
                r.content,
                r.createdAt
                )
            FROM Review r
            JOIN r.matching m
            JOIN m.user u
            WHERE m.trainerProfile.id = :trainerId
            ORDER BY r.createdAt DESC
            """)
    Page<com.solo.ptmatch.review.presentation.response.TrainerReviewSummaryResponse> findTrainerReviews(Long trainerId,
            Pageable pageable);

    boolean existsByMatching(Matching matching);

    Optional<Review> findByIdAndMatchingUserId(Long reviewId, Long userId);
}
