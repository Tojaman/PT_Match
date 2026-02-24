package com.solo.ptmatch.user.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.user.application.dto.AuthResult;
import com.solo.ptmatch.user.infrastructure.redis.RedisRefreshTokenStore;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
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
    private final RedisRefreshTokenStore refreshTokenStore;
    private final TrainerProfileRepository trainerProfileRepository;

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

        Duration refreshTokenTtl = Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidityMillis());
        refreshTokenStore.save(user.getEmail(), refreshToken, refreshTokenTtl);

        Long trainerId = null;
        if (user.getRole() == Role.TRAINER) {
            trainerId = trainerProfileRepository.findByUserId(user.getId())
                    .map(TrainerProfile::getId)
                    .orElse(null);
        }

        return AuthResult.from(user, accessToken, refreshToken, trainerId);
    }

    @Transactional
    public void logout(String email) {
        refreshTokenStore.deleteByEmail(email);
    }

    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw GlobalException.of(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.extractSubject(refreshToken);

        String savedRefreshTokenHash = refreshTokenStore.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        String requestRefreshTokenHash = hash(refreshToken);
        if (!savedRefreshTokenHash.equals(requestRefreshTokenHash)) {
            throw GlobalException.of(ErrorCode.INVALID_TOKEN);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        return jwtTokenProvider.generateAccessToken(authentication.getName(), authentication.getAuthorities());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to hash refresh token.", e);
            throw GlobalException.of(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
