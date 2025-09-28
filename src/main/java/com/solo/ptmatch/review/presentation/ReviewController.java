package com.solo.ptmatch.review.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.review.application.ReviewService;
import com.solo.ptmatch.review.presentation.request.ReviewCreateRequest;
import com.solo.ptmatch.review.presentation.response.MyReviewSummaryResponse;
import com.solo.ptmatch.review.presentation.response.ReviewCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "후기 작성", description = "사용자가 매칭에 대한 후기를 작성한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "후기 작성 성공")
    @PostMapping("/reviews")
    public ApiResponse<ReviewCreateResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        ReviewCreateResponse response = reviewService.createReview(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 후기 목록", description = "사용자가 작성한 후기 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 후기 목록 조회 성공")
    @GetMapping("/me/reviews")
    public ApiResponse<List<MyReviewSummaryResponse>> getMyReviews() {
        List<MyReviewSummaryResponse> response = reviewService.getMyReviews();
        return ApiResponse.success(response);
    }
}
