package com.solo.ptmatch.user.application.dto;

import com.solo.ptmatch.user.domain.Role;

public record RegisterCommand(
    String email,
    String password,
    String name,
    Role role
) {
}
