package com.solo.ptmatch.user.presentation.response;

import com.solo.ptmatch.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record RegisterResponse(
    @Schema(description = "회원 ID", example = "1")
    Long userId,
    @Schema(description = "회원 이메일", example = "user@example.com")
    String email,
    @Schema(description = "회원 이름", example = "김헬스")
    String name,
    @Schema(description = "회원 역할", example = "USER")
    Role role
) {
}
