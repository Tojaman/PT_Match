package com.solo.ptmatch.user.application;

import com.solo.ptmatch.user.application.dto.LoginCommand;
import com.solo.ptmatch.user.application.dto.LoginResult;
import com.solo.ptmatch.user.application.dto.RegisterCommand;
import com.solo.ptmatch.user.application.dto.RegisterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    public RegisterResult register(RegisterCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LoginResult login(LoginCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
