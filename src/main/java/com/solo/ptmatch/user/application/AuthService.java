package com.solo.ptmatch.user.application;

import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.user.presentation.response.LoginResponse;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    public RegisterResponse register(RegisterRequest command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LoginResponse login(LoginRequest command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
