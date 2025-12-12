package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.common.response.PageResponse;
import com.solo.ptmatch.trainer.application.TrainerLikeService;
import com.solo.ptmatch.trainer.presentation.response.LikedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerLikeToggleResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TrainerLikeController {

    private final TrainerLikeService trainerLikeService;

    @Operation(summary = "트레이너 좋아요 토글", description = "사용자가 트레이너 좋아요 상태를 토글한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 토글 성공")
    @PostMapping("/trainers/{trainerId}/follow")
    public ResponseEntity<ApiResponse<TrainerLikeToggleResponse>> toggleFollow(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @PathVariable Long trainerId) {
        TrainerLikeToggleResponse response = trainerLikeService.toggleFollow(trainerId, loggedInEmail);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "좋아요한 트레이너 목록", description = "사용자가 좋아요한 트레이너 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요한 트레이너 목록 조회 성공")
    @GetMapping("/me/follows/trainers")
    public ResponseEntity<ApiResponse<List<LikedTrainerSummaryResponse>>> getFollowedTrainers(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LikedTrainerSummaryResponse> response = trainerLikeService.getFollowedTrainers(loggedInEmail, pageable);
        return ResponseEntity.ok(ApiResponse.success(response.getContent(), PageResponse.from(response)));
    }

    @Operation(summary = "좋아요 수", description = "트레이너가 좋아요 수를 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 수 조회 성공")
    @GetMapping("/me/followers/count")
    public ResponseEntity<ApiResponse<Long>> getFollowedUserCount(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail
    ) {
        long followedUserCount = trainerLikeService.getFollowedUserCount(loggedInEmail);
        return ResponseEntity.ok(ApiResponse.success(followedUserCount));
    }
}
