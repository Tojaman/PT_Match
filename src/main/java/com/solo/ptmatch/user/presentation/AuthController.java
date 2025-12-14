package com.solo.ptmatch.user.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.user.application.dto.AuthResult;
import com.solo.ptmatch.user.application.AuthService;
import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.user.presentation.response.LoginResponse;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;

        @Operation(summary = "회원가입", description = "이메일과 비밀번호, 역할 정보를 입력받아 신규 회원을 등록한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공")
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
                RegisterResponse response = authService.register(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response));
        }

        @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력받아 로그인한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공")
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
                AuthResult result = authService.login(request);

                ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", result.accessToken())
                                .httpOnly(true)
                                .secure(false)
                                .path("/")
                                .maxAge(3600) // 1시간
                                .sameSite("Lax")
                                .build();

                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                                .httpOnly(true)
                                .secure(false)
                                .path("/api/auth/refresh")
                                .maxAge(1209600) // 14일
                                .sameSite("Lax")
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                .body(ApiResponse.success(LoginResponse.from(result)));
        }

        @Operation(summary = "로그아웃", description = "로그아웃한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "로그아웃 성공")
        @PostMapping("/logout")
        public ResponseEntity<Void> logout(Authentication authentication) {
                if (authentication != null) {
                        authService.logout(authentication.getName());
                }

                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .secure(false)
                                .path("/api/auth/refresh")
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();

                ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                                .httpOnly(true)
                                .secure(false)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();

                return ResponseEntity.noContent()
                                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                                .build();
        }

        @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 엑세스 토큰을 재발급한다.")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "토큰 재발급 성공")
        @PostMapping("/refresh")
        public ResponseEntity<Void> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
                if (refreshToken == null) {
                        return ResponseEntity.status(401).build();
                }
                String newAccessToken = authService.refreshAccessToken(refreshToken);

                ResponseCookie cookie = ResponseCookie.from("accessToken", newAccessToken)
                                .httpOnly(true)
                                .secure(false)
                                .path("/")
                                .maxAge(3600) // 1시간
                                .sameSite("Lax")
                                .build();

                return ResponseEntity.noContent()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .build();
        }
}
