package com.solo.ptmatch.user.presentation.response;

import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Role role;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole());
    }
}
