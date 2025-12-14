package com.solo.ptmatch.user.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.user.domain.RefreshToken;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.RefreshTokenRepository;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import com.solo.ptmatch.user.application.dto.AuthResult;
import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            GlobalException.throwError(ErrorCode.USER_EMAIL_DUPLICATED);
        }

        User user = User.create(
                registerRequest.email(),
                passwordEncoder.encode(registerRequest.password()),
                registerRequest.name(),
                registerRequest.phoneNumber(),
                registerRequest.role());

        User savedUser = userRepository.save(user);
        return RegisterResponse.from(savedUser);
    }

    @Transactional
    public AuthResult login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.email(),
                loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication.getName(),
                authentication.getAuthorities());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName(),
                authentication.getAuthorities());

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.save(RefreshToken.of(user.getEmail(), refreshToken));

        return AuthResult.from(user, accessToken, refreshToken);
    }

    @Transactional
    public void logout(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw GlobalException.of(ErrorCode.INVALID_TOKEN);
        }

        // DB에서 리프레시 토큰 확인
        refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> GlobalException.of(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        return jwtTokenProvider.generateAccessToken(authentication.getName(), authentication.getAuthorities());
    }
}
