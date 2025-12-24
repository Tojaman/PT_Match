package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.TrainerScheduleService;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleCreateRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleDeleteRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerScheduleUpdateRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainers/schedules")
public class TrainerScheduleController {

    private final TrainerScheduleService trainerScheduleService;

    @Operation(summary = "트레이너 스케줄 등록", description = "트레이너 자신의 스케줄을 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "스케줄 등록 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping
    public ResponseEntity<ApiResponse<List<TrainerScheduleListResponse>>> scheduleRegister(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerScheduleCreateRequest trainerScheduleCreateRequest
    ) {

        List<TrainerScheduleListResponse> response = trainerScheduleService.registerTrainerSchedule(
                loggedInEmail,
                trainerScheduleCreateRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "트레이너 스케줄 삭제", description = "트레이너 자신의 스케줄을 삭제한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "스케줄 삭제 성공")
    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/schedule")
    public ResponseEntity<Void> deleteTrainerSchedule(
            @AuthenticationPrincipal(expression = "username") String loggedInEmail,
            @Valid @RequestBody TrainerScheduleDeleteRequest trainerScheduleDeleteRequest
    ) {
        trainerScheduleService.deleteTrainerSchedule(loggedInEmail, trainerScheduleDeleteRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "트레이너 스케줄 조회", description = "트레이너 스케줄을 조회한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스케줄 조회 성공")
    @GetMapping("/{trainerId}")
    public ResponseEntity<ApiResponse<List<TrainerScheduleListResponse>>> getTrainerSchedules(
            @PathVariable Long trainerId
    ) {
        List<TrainerScheduleListResponse> response = trainerScheduleService.getTrainerSchedules(trainerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
