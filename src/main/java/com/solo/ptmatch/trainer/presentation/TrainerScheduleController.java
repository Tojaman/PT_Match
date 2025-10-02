package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.TrainerScheduleService;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleDeleteRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleListRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainers/schedules")
public class TrainerScheduleController {

    private final TrainerScheduleService trainerProfileService;

    @Operation(summary = "트레이너 스케줄 등록", description = "트레이너 자신의 스케줄을 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 등록 성공")
    @PostMapping
    public ApiResponse<TrainerScheduleListResponse> scheduleRegister(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerScheduleListRequest trainerScheduleListRequest
    ) {

        TrainerScheduleListResponse response = trainerProfileService.registerTrainerSchedule(
                loggedInEmail,
                trainerScheduleListRequest
        );
        return ApiResponse.success(response);
    }

//    @Operation(summary = "트레이너 스케줄 수정", description = "트레이너 자신의 스케줄을 수정한다")
//    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 수정 성공")
//    @PutMapping("/schedule")
//    public ApiResponse<TrainerScheduleListResponse> updateTrainerSchedule(
//            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
//            @Valid @RequestBody TrainerScheduleUpdateRequest trainerScheduleUpdateRequest
//    ) {
//
//
//    }

//    @Operation(summary = "트레이너 스케줄 삭제", description = "트레이너 자신의 스케줄을 삭제한다")
//    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 삭제 성공")
//    @DeleteMapping("/schedule")
//    public ApiResponse<TrainerScheduleListResponse> deleteTrainerSchedule(
//            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
//            @Valid @RequestBody TrainerScheduleDeleteRequest trainerScheduleDeleteRequest
//    ) {
//
//
//    }
}
