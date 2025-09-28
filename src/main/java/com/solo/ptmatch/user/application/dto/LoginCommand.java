package com.solo.ptmatch.user.application.dto;

public record LoginCommand(
    String email,
    String password
) {
}
