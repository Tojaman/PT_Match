package com.solo.ptmatch.user.presentation.response;

import com.solo.ptmatch.user.application.dto.LoginResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
    @Schema(description = "JWT 액세스 토큰", example = "jwt.token.string")
    String accessToken
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken());
    }
}
