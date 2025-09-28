package com.solo.ptmatch.user.application.dto;

import com.solo.ptmatch.user.domain.UserRole;

public record RegisterResult(
    Long userId,
    String email,
    String name,
    UserRole role
) {
}
