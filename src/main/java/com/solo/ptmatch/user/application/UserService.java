package com.solo.ptmatch.user.application;

import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import com.solo.ptmatch.user.presentation.request.UserUpdateRequest;
import com.solo.ptmatch.user.presentation.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse updateProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = user.getPassword(); // 기본값: 기존 비밀번호

        // 비밀번호 변경 요청이 있는 경우
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            // 현재 비밀번호 검증
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                throw GlobalException.of(ErrorCode.PASSWORD_NOT_MATCH);
            }
            encodedPassword = passwordEncoder.encode(request.newPassword());
        }

        user.updateProfile(request.name(), encodedPassword, request.phoneNumber());

        return UserResponse.from(user);
    }

    public UserResponse getUserDetail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public void verifyPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw GlobalException.of(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }
}
