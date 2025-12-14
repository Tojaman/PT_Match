package com.solo.ptmatch.user.presentation.response;

import com.solo.ptmatch.user.application.dto.AuthResult;
import com.solo.ptmatch.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
        Long userId,
        String email,
        String name,
        Role role) {
    public static LoginResponse from(AuthResult authResult) {
        return new LoginResponse(
                authResult.userId(),
                authResult.email(),
                authResult.name(),
                authResult.role());
    }
}
