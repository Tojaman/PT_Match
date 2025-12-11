package com.solo.ptmatch.user.presentation.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하이어야 합니다.")
    private String name;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    private String phoneNumber;
}
