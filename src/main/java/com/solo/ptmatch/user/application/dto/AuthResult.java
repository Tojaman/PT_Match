package com.solo.ptmatch.user.application.dto;

import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;

public record AuthResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String name,
        Role role,
        Long trainerId) {
    public static AuthResult of(String accessToken, String refreshToken, Long userId, String email, String name,
            Role role, Long trainerId) {
        return new AuthResult(accessToken, refreshToken, userId, email, name, role, trainerId);
    }

    public static AuthResult from(User user, String accessToken, String refreshToken, Long trainerId) {
        return new AuthResult(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                trainerId);
    }
}
