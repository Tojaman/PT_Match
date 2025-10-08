package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.TrainerScheduleService;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleDeleteRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleCreateRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainers/schedules")
public class TrainerScheduleController {

    private final TrainerScheduleService trainerScheduleService;

    @Operation(summary = "트레이너 스케줄 등록", description = "트레이너 자신의 스케줄을 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 등록 성공")
    @PostMapping
    public ApiResponse<List<TrainerScheduleListResponse>> scheduleRegister(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerScheduleCreateRequest trainerScheduleCreateRequest
    ) {

        List<TrainerScheduleListResponse> response = trainerScheduleService.registerTrainerSchedule(
                loggedInEmail,
                trainerScheduleCreateRequest
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "트레이너 스케줄 수정", description = "트레이너 자신의 스케줄을 수정한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 수정 성공")
    @PatchMapping
    public ApiResponse<List<TrainerScheduleListResponse>> updateTrainerSchedule(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerScheduleUpdateRequest trainerScheduleUpdateRequest
    ) {

        List<TrainerScheduleListResponse> response = trainerScheduleService.updateTrainerSchedule(
                loggedInEmail,
                trainerScheduleUpdateRequest
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "트레이너 스케줄 삭제", description = "트레이너 자신의 스케줄을 삭제한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "스케줄 삭제 성공")
    @DeleteMapping("/schedule")
    public ApiResponse<Void> deleteTrainerSchedule(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerScheduleDeleteRequest trainerScheduleDeleteRequest
    ) {
        trainerScheduleService.deleteTrainerSchedule(loggedInEmail, trainerScheduleDeleteRequest);
        return ApiResponse.success();
    }

    @Operation(summary = "트레이너 스케줄 조회", description = "트레이너 스케줄을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 조회 성공")
    @GetMapping("/{trainerId}")
    public ApiResponse<List<TrainerScheduleListResponse>> getTrainerSchedules(
            @PathVariable Long trainerId
    ) {
        List<TrainerScheduleListResponse> response = trainerScheduleService.getTrainerSchedules(trainerId);
        return ApiResponse.success(response);
    }
}
