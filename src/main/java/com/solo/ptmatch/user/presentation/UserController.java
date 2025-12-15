package com.solo.ptmatch.user.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.user.application.UserService;
import com.solo.ptmatch.user.presentation.request.PasswordVerifyRequest;
import com.solo.ptmatch.user.presentation.request.UserUpdateRequest;
import com.solo.ptmatch.user.presentation.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 상세 정보를 조회한다")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal(expression = "username") String email) {
        UserResponse response = userService.getUserDetail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 정보를 수정한다")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 검증", description = "내 정보 수정 전 비밀번호를 검증한다")
    @PostMapping("/me/verification")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody PasswordVerifyRequest request) {
        log.info("비밀번호 검증 수신");
        userService.verifyPassword(email, request.password());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
