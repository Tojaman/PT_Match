package com.solo.ptmatch.review.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.review.domain.Review;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.review.presentation.request.ReviewCreateRequest;
import com.solo.ptmatch.review.presentation.request.ReviewUpdateRequest;
import com.solo.ptmatch.review.presentation.response.*;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Page<MyReviewSummaryResponse> getMyReviews(String userEmail, Pageable pageable) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findUserReviews(user.getId(), pageable);
        return reviews.map(MyReviewSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TrainerReviewSummaryResponse> getTrainerReviews(Long trainerId, Pageable pageable) {

        Page<Review> reviews = reviewRepository.findTrainerReviews(trainerId, pageable);
        return reviews.map(TrainerReviewSummaryResponse::from);
    }

    @Transactional
    public ReviewCreateResponse createReview(String userEmail, ReviewCreateRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Matching matching = matchingRepository.findByIdAndUserId(request.matchingId(), user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_NOT_FOUND));

        if (reviewRepository.existsByMatching(matching)) {
            throw GlobalException.of(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.create(matching, request.rating(), request.content());
        // 평균 평점 계산 및 리뷰 수 증가
        matching.getTrainerProfile().addReviewRating(request.rating());

        reviewRepository.save(review);

        return ReviewCreateResponse.of(review.getId());
    }

    @Transactional
    public ReviewUpdateResponse updateReview(String userEmail, Long reviewId, ReviewUpdateRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findByIdAndMatchingUserId(reviewId, user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.REVIEW_NOT_FOUND));

        review.getMatching().getTrainerProfile().updateReviewRating(review.getRating(), request.rating());
        review.update(request.rating(), request.content());

        return ReviewUpdateResponse.from(review);
    }

    @Transactional
    public void deleteReview(String userEmail, Long reviewId) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findByIdAndMatchingUserId(reviewId, user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.REVIEW_NOT_FOUND));

        review.getMatching().getTrainerProfile().deleteReviewRating(review.getRating());

        reviewRepository.delete(review);
    }
}