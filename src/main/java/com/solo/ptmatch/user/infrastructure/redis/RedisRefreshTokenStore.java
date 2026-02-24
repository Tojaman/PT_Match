package com.solo.ptmatch.user.infrastructure.redis;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore {

    private static final String REFRESH_TOKEN_KEY_RULE = "auth:refresh:{email}";
    private static final String EMAIL_PLACEHOLDER = "{email}";

    private final StringRedisTemplate stringRedisTemplate;

    public void save(String email, String refreshToken, Duration ttl) {
        String key = toKey(email);
        String hashedRefreshToken = hash(refreshToken);

        try {
            stringRedisTemplate.opsForValue().set(key, hashedRefreshToken, ttl);
        } catch (Exception e) {
            log.error("Failed to save refresh token in Redis. key={}", key, e);
            throw GlobalException.of(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<String> findByEmail(String email) {
        String key = toKey(email);

        try {
            return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("Failed to find refresh token in Redis. key={}", key, e);
            throw GlobalException.of(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteByEmail(String email) {
        String key = toKey(email);

        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete refresh token in Redis. key={}", key, e);
            throw GlobalException.of(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String toKey(String email) {
        return REFRESH_TOKEN_KEY_RULE.replace(EMAIL_PLACEHOLDER, email);
    }

    private String hash(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to hash refresh token.", e);
            throw GlobalException.of(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
