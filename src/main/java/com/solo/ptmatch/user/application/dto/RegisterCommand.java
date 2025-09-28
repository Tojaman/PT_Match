package com.solo.ptmatch.user.application.dto;

import com.solo.ptmatch.user.domain.UserRole;

public record RegisterCommand(
    String email,
    String password,
    String name,
    UserRole role
) {
}
