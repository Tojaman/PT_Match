package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.TrainerFollowService;
import com.solo.ptmatch.trainer.presentation.response.FollowedTrainerSummaryResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerFollowToggleResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TrainerFollowController {

    private final TrainerFollowService trainerFollowService;

    @Operation(summary = "트레이너 팔로우 토글", description = "사용자가 트레이너 팔로우 상태를 토글한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로우 토글 성공")
    @PostMapping("/trainers/{trainerId}/follow")
    public ApiResponse<TrainerFollowToggleResponse> toggleFollow(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @PathVariable Long trainerId) {
        TrainerFollowToggleResponse response = trainerFollowService.toggleFollow(trainerId, loggedInEmail);
        return ApiResponse.success(response);
    }

    @Operation(summary = "팔로우한 트레이너 목록", description = "사용자가 팔로우한 트레이너 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로우한 트레이너 목록 조회 성공")
    @GetMapping("/me/follows/trainers")
    public ApiResponse<List<FollowedTrainerSummaryResponse>> getFollowedTrainers(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail
    ) {
        List<FollowedTrainerSummaryResponse> response = trainerFollowService.getFollowedTrainers(loggedInEmail);
        log.info("결과: {}", response);
        return ApiResponse.success(response);
    }
}
