package com.solo.ptmatch.user.presentation.request;

import com.solo.ptmatch.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record RegisterRequest(
    @Schema(description = "로그인 이메일", example = "user@example.com")
    @NotBlank
    @Email
    String email,

    @Schema(description = "로그인 비밀번호", example = "password123")
    @NotBlank
    @Size(min = 8, max = 64)
    String password,

    @Schema(description = "회원 이름", example = "김헬스")
    @NotBlank
    @Size(min = 2, max = 30)
    String name,

    @Schema(description = "회원 전화번호", example = "01012345678")
    @NotBlank
    @Size(min = 10, max = 11)
    String phoneNumber,

    @Schema(description = "사용자 역할", example = "USER", allowableValues = {"USER", "TRAINER"})
    @NotNull
    Role role
) {
}
