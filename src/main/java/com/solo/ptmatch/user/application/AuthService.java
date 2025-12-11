package com.solo.ptmatch.user.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            GlobalException.throwError(ErrorCode.USER_EMAIL_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.password());
        User user = User.create(
            registerRequest.email(),
            encodedPassword,
            registerRequest.name(),
            registerRequest.phoneNumber(),
            registerRequest.role()
        );

        User savedUser = userRepository.save(user);
        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getRole()
        );
    }
}
