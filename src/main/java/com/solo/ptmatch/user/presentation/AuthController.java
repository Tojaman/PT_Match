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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
        private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
        private static final String COOKIE_PATH = "/";
        private static final String COOKIE_SAME_SITE = "Lax";

        private final AuthService authService;
        @Value("${security.jwt.access-token-validity-ms}")
        private long accessTokenValidityMillis;
        @Value("${security.jwt.refresh-token-validity-ms}")
        private long refreshTokenValidityMillis;
        @Value("${security.cookie.secure:false}")
        private boolean cookieSecure;

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

                ResponseCookie accessTokenCookie = buildAccessTokenCookie(result.accessToken());
                ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(result.refreshToken());

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

                return ResponseEntity.ok()
                                .headers(headers)
                                .body(ApiResponse.success(LoginResponse.from(result)));
        }

        @Operation(summary = "로그아웃", description = "로그아웃한다")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "로그아웃 성공")
        @PostMapping("/logout")
        public ResponseEntity<Void> logout(Authentication authentication) {
                if (authentication != null) {
                        authService.logout(authentication.getName());
                }

                ResponseCookie refreshTokenCookie = expireCookie(REFRESH_TOKEN_COOKIE_NAME);
                ResponseCookie accessTokenCookie = expireCookie(ACCESS_TOKEN_COOKIE_NAME);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

                return ResponseEntity.noContent()
                                .headers(headers)
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

                ResponseCookie cookie = buildAccessTokenCookie(newAccessToken);

                return ResponseEntity.noContent()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .build();
        }

        private ResponseCookie buildAccessTokenCookie(String tokenValue) {
                return buildCookie(
                                ACCESS_TOKEN_COOKIE_NAME,
                                tokenValue,
                                TimeUnit.MILLISECONDS.toSeconds(accessTokenValidityMillis));
        }

        private ResponseCookie buildRefreshTokenCookie(String tokenValue) {
                return buildCookie(
                                REFRESH_TOKEN_COOKIE_NAME,
                                tokenValue,
                                TimeUnit.MILLISECONDS.toSeconds(refreshTokenValidityMillis));
        }

        private ResponseCookie expireCookie(String cookieName) {
                return buildCookie(cookieName, "", 0);
        }

        private ResponseCookie buildCookie(String cookieName, String value, long maxAgeSeconds) {
                return ResponseCookie.from(cookieName, value)
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .path(COOKIE_PATH)
                                .maxAge(maxAgeSeconds)
                                .sameSite(COOKIE_SAME_SITE)
                                .build();
        }
}
