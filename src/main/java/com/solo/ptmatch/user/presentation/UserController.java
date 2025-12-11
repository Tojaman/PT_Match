package com.solo.ptmatch.user.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.user.application.UserService;
import com.solo.ptmatch.user.presentation.request.UserUpdateRequest;
import com.solo.ptmatch.user.presentation.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 정보를 수정한다")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
