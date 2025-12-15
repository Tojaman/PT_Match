package com.solo.ptmatch.user.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest (

    @Schema(description = "이름", example = "김헬스")
    String name,

    @Schema(description = "기존 비밀번호", example = "password123")
    String password,

    // @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Schema(description = "새 비밀번호", example = "newPassword123")
    String newPassword,

    @Schema(description = "연락처", example = "01012345678")
    String phoneNumber
) {

}