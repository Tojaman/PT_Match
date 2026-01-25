package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.common.response.PageResponse;
import com.solo.ptmatch.product.presentation.request.PresignedUrlRequest;
import com.solo.ptmatch.product.presentation.response.PresignedUrlResponse;
import com.solo.ptmatch.trainer.application.TrainerProfileService;
import com.solo.ptmatch.trainer.presentation.request.TrainerProfileUpsertRequest;
import com.solo.ptmatch.trainer.presentation.request.TrainerSearchRequest;
import com.solo.ptmatch.trainer.presentation.response.TrainerDetailResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerProfileUpsertResponse;
import com.solo.ptmatch.trainer.presentation.response.TrainerSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainers")
public class TrainerProfileController {

        private final TrainerProfileService trainerProfileService;

        @Operation(summary = "지하철 역, 법정동, 헬스장 이름으로 검색", description = "명확한 타입 기반 검색")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목록 조회 성공")
        @GetMapping
        public ResponseEntity<ApiResponse<List<TrainerSummaryResponse>>> getTrainers(
                        @Valid @ModelAttribute TrainerSearchRequest trainerSearchRequest,
                        @ParameterObject @PageableDefault(size = 10, sort = "averageRating", direction = Sort.Direction.DESC) Pageable pageable) {
                Page<TrainerSummaryResponse> response = trainerProfileService.getTrainerSummaries(trainerSearchRequest,
                                pageable);
                return ResponseEntity.ok(ApiResponse.success(response.getContent(), PageResponse.from(response)));
        }

        @Operation(summary = "인기 트레이너 조회", description = "위치 기반 인기 트레이너 목록 (인기도 점수 정렬)")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인기 트레이너 조회 성공")
        @GetMapping("/popular")
        public ResponseEntity<ApiResponse<List<TrainerSummaryResponse>>> getPopularTrainers(
                        @RequestParam double lat,
                        @RequestParam double lon,
                        @RequestParam(defaultValue = "10000") double radiusMeters,
                        @RequestParam(defaultValue = "8") int limit) {
                List<TrainerSummaryResponse> trainers = trainerProfileService.getPopularTrainers(lat, lon, radiusMeters, limit);
                return ResponseEntity.ok(ApiResponse.success(trainers));
        }

        @Operation(summary = "내 트레이너 프로필 조회", description = "트레이너 자신의 상세 정보를 조회한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상세 조회 성공")
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<TrainerDetailResponse>> getMyTrainer(
                        @AuthenticationPrincipal(expression = "username") String loggedInEmail) {

                log.info("유저 이메일: {}", loggedInEmail);
                Long trainerId = trainerProfileService.getTrainerId(loggedInEmail);
                TrainerDetailResponse response = trainerProfileService.getTrainerDetail(trainerId);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "트레이너 상세 조회", description = "특정 트레이너의 상세 정보를 조회한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상세 조회 성공")
        @GetMapping("/{trainerId}")
        public ResponseEntity<ApiResponse<TrainerDetailResponse>> getTrainer(
                        @PathVariable Long trainerId) {
                TrainerDetailResponse response = trainerProfileService.getTrainerDetail(trainerId);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "트레이너 프로필 등록", description = "트레이너 자신의 프로필을 신규 등록한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "프로필 등록 성공")
        @PostMapping("/me")
        public ResponseEntity<ApiResponse<TrainerProfileUpsertResponse>> register(
                        @AuthenticationPrincipal(expression = "username") String loggedInEmail,
                        @Valid @RequestBody TrainerProfileUpsertRequest trainerProfileRegisterRequest) {

                TrainerProfileUpsertResponse response = trainerProfileService.registerTrainerProfile(
                                loggedInEmail,
                                trainerProfileRegisterRequest);
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
        }

        @Operation(summary = "트레이너 프로필 수정", description = "트레이너 자신의 프로필을 수정한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공")
        @PatchMapping("/me")
        public ResponseEntity<ApiResponse<TrainerProfileUpsertResponse>> update(
                        @AuthenticationPrincipal(expression = "username") String loggedInEmail,
                        @Valid @RequestBody TrainerProfileUpsertRequest trainerProfileRegisterRequest) {

                TrainerProfileUpsertResponse response = trainerProfileService.updateTrainerProfile(
                                loggedInEmail,
                                trainerProfileRegisterRequest);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "트레이너 프로필 이미지 업로드용 프리사인 URL 발급", description = "트레이너가 프로필 이미지 업로드를 위한 프리사인 URL을 발급받는다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프리사인 URL 발급 성공")
        @PreAuthorize("hasRole('TRAINER')")
        @PostMapping("/images/presign")
        public ResponseEntity<ApiResponse<PresignedUrlResponse>> issuePresignedUrl(
                        @AuthenticationPrincipal(expression = "username") String loggedInEmail,
                        @Valid @RequestBody PresignedUrlRequest request) {
                PresignedUrlResponse response = trainerProfileService.issuePresignedUrl(request, loggedInEmail);
                return ResponseEntity.ok(ApiResponse.success(response));
        }
}
