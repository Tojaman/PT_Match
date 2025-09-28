package com.solo.ptmatch.user.presentation.request;

import com.solo.ptmatch.user.application.dto.LoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
    @Schema(description = "로그인 이메일", example = "user@example.com")
    @NotBlank
    @Email
    String email,

    @Schema(description = "로그인 비밀번호", example = "password123")
    @NotBlank
    String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
