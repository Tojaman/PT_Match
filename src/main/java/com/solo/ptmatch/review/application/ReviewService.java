package com.solo.ptmatch.review.application;

import com.solo.ptmatch.review.presentation.request.ReviewCreateRequest;
import com.solo.ptmatch.review.presentation.response.MyReviewSummaryResponse;
import com.solo.ptmatch.review.presentation.response.ReviewCreateResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReviewService {

    public ReviewCreateResponse createReview(ReviewCreateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<MyReviewSummaryResponse> getMyReviews() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
