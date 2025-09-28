package com.solo.ptmatch.user.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.user.application.AuthService;
import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.user.presentation.response.LoginResponse;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일과 비밀번호, 역할 정보를 입력받아 신규 회원을 등록한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공")
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = RegisterResponse.from(authService.register(request.toCommand()));
        return ApiResponse.success(response);
    }

    @Operation(summary = "로그인", description = "사용자 인증에 성공하면 JWT 액세스 토큰을 반환한다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = LoginResponse.from(authService.login(request.toCommand()));
        return ApiResponse.success(response);
    }
}
