package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.ProgramService;
import com.solo.ptmatch.trainer.presentation.request.TrainerProgramUpsertRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerProgramResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/programs")
public class ProgramController {
    private final ProgramService programService;

    @Operation(summary = "프로그램 등록", description = "트레이너 자신의 프로그램을 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로그램 등록 성공")
    @PostMapping
    public ResponseEntity<ApiResponse<TrainerProgramResponse>> registerProgram(
            @Valid @RequestBody TrainerProgramUpsertRequest trainerProgramRequest,
            @AuthenticationPrincipal(expression = "username") String email) {

        TrainerProgramResponse response = programService.registerProgram(email, trainerProgramRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "프로그램 수정", description = "트레이너 자신의 프로그램을 수정한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로그램 수정 성공")
    @PutMapping("/{programId}")
    public ResponseEntity<ApiResponse<TrainerProgramResponse>> updateProgram(
            @PathVariable Long programId,
            @Valid @RequestBody TrainerProgramUpsertRequest trainerProgramRequest,
            @AuthenticationPrincipal(expression = "username") String email) {

        TrainerProgramResponse response = programService.updateProgram(email, programId, trainerProgramRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "프로그램 삭제", description = "트레이너 자신의 프로그램을 삭제한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로그램 삭제 성공")
    @DeleteMapping("/{programId}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(
            @PathVariable Long programId,
            @AuthenticationPrincipal(expression = "username") String email) {

        programService.deleteProgram(email, programId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
