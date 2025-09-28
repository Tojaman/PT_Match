package com.solo.ptmatch.user.application.dto;

import com.solo.ptmatch.user.domain.Role;

public record RegisterResult(
    Long userId,
    String email,
    String name,
    Role role
) {
}
