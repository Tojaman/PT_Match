package com.solo.ptmatch.user.presentation.response;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
    @Schema(description = "JWT 액세스 토큰", example = "jwt.token.string")
    String accessToken
) {
}
