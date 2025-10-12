package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.common.response.PageResponse;
import com.solo.ptmatch.trainer.application.TrainerProfileService;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerProfileService trainerProfileService;

    @Operation(summary = "트레이너 목록 조회", description = "필터 및 정렬 조건으로 트레이너 목록을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목록 조회 성공")
    @GetMapping
    public ApiResponse<List<TrainerSummaryResponse>> getTrainers(
            @Valid @ModelAttribute TrainerSearchRequest trainerSearchRequest,
            @ParameterObject @PageableDefault(size = 10, sort = "averageRating", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TrainerSummaryResponse> response = trainerProfileService.getTrainerSummaries(trainerSearchRequest, pageable);
        return ApiResponse.success(response.getContent(), PageResponse.from(response));
    }

    @Operation(summary = "트레이너 상세 조회", description = "특정 트레이너의 상세 정보를 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상세 조회 성공")
    @GetMapping("/{trainerId}")
    public ApiResponse<TrainerDetailResponse> getTrainer(
            @PathVariable Long trainerId) {
        TrainerDetailResponse response = trainerProfileService.getTrainerDetail(trainerId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "트레이너 프로필 등록", description = "트레이너 자신의 프로필을 신규 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 등록 성공")
    @PostMapping("/me")
    public ApiResponse<TrainerProfileUpsertResponse> register(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerProfileUpsertRequest trainerProfileRegisterRequest
    ) {

        TrainerProfileUpsertResponse response = trainerProfileService.registerTrainerProfile(
                loggedInEmail,
                trainerProfileRegisterRequest
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "트레이너 프로필 수정", description = "트레이너 자신의 프로필을 수정한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공")
    @PutMapping("/me")
    public ApiResponse<TrainerProfileUpsertResponse> update(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerProfileUpsertRequest trainerProfileRegisterRequest
    ) {

        TrainerProfileUpsertResponse response = trainerProfileService.updateTrainerProfile(
                loggedInEmail,
                trainerProfileRegisterRequest
        );
        return ApiResponse.success(response);
    }
}
