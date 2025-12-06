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

    @Transactional
    public ReviewCreateResponse createReview(String userEmail, ReviewCreateRequest request) {
        /*
         * 1. 사용자 조회
         * 2. 매칭 조회 (WHERE user.id = user_id)
         * 3. 리뷰 작성된지 검증(리뷰는 한 개만 작성 가능)
         * 4. Review.save(Review.create()) and 매칭된 트레이너의 리뷰 개수 +1
         */
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Matching matching = matchingRepository.findByIdAndUserId(request.matchingId(), user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_NOT_FOUND));

        if (reviewRepository.existsByMatching(matching)) {
            throw GlobalException.of(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.create(matching, request.rating(), request.content());
        matching.getTrainerProfile().increaseReviewCount();
        reviewRepository.save(review);

        return ReviewCreateResponse.of(review.getId());
    }

    @Transactional(readOnly = true)
    public Page<MyReviewSummaryResponse> getMyReviews(String userEmail, int page, int size) {
        /*
         * 1. 사용자 조회
         * 2. 사용자의 리뷰 조회 - 조인(Matching, Product)해서 상품 제목 가지고 옴
         * 3. 조회한 리뷰를 MyReviewsResponse로 변환해서 반환
         */
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);

        return reviewRepository.findUserReviews(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductReviewSummaryResponse> getProductReviews(Long productId, int page, int size) {
        /*
         * 1. 상품에 해당하는 모든 리뷰 조회
         * (SELECT Review r, u.name FROM Review r JOIN Matching m JOIN User u WHERE
         * m.product.id = productId
         * 2. 리뷰를 ProductReviewsResponse로 변환해서 반환
         * 만약 리뷰가 없다면 빈 data[] 반환
         */

        Pageable pageable = PageRequest.of(page, size);

        return reviewRepository.findProductReview(productId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TrainerReviewSummaryResponse> getTrainerReviews(Long trainerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findTrainerReviews(trainerId, pageable);
    }

    @Transactional
    public ReviewUpdateResponse updateReview(String userEmail, Long reviewId, ReviewUpdateRequest request) {
        /*
         * 1. 유저 조회
         * 2. 유저 리뷰 조회(유저 리뷰만 조회)
         * 3. 업데이트
         */

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findByIdAndMatchingUserId(reviewId, user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.REVIEW_NOT_FOUND));

        review.update(request.rating(), request.content());
        return ReviewUpdateResponse.from(review);
    }

    @Transactional
    public void deleteReview(String userEmail, Long reviewId) {
        /*
         * 1. 유저 조회
         * 2. 유저 리뷰 조회(유저 리뷰만 조회)
         * 3. 삭제
         */

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findByIdAndMatchingUserId(reviewId, user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.REVIEW_NOT_FOUND));

        reviewRepository.delete(review);
    }
}