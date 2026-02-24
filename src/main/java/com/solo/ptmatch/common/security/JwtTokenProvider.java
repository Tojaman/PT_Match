package com.solo.ptmatch.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "roles";

    private final SecretKey secretKey;
    private final long accessTokenValidityMillis;
    private final long refreshTokenValidityMillis;

    // JWT 시크릿 키와 토큰 유효기간을 설정하는 생성자
    public JwtTokenProvider(
            @Value("${security.jwt.secret:}") String secret,
            @Value("${security.jwt.access-token-validity-ms:3600000}") long accessTokenValidityMillis,
            @Value("${security.jwt.refresh-token-validity-ms:1209600000}") long refreshTokenValidityMillis) {
        byte[] keyBytes = decodeSecret(secret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes)");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityMillis = accessTokenValidityMillis;
        this.refreshTokenValidityMillis = refreshTokenValidityMillis;
    }

    public String generateAccessToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(subject, authorities, accessTokenValidityMillis);
    }

    public String generateRefreshToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(subject, authorities, refreshTokenValidityMillis);
    }

    public long getRefreshTokenValidityMillis() {
        return refreshTokenValidityMillis;
    }

    private String generateToken(String subject, Collection<? extends GrantedAuthority> authorities,
            long validityMillis) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityMillis);
        List<String> roles = authorities == null ? List.of()
                : authorities.stream()
                        .filter(Objects::nonNull)
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(subject)
                .claim(AUTHORITIES_KEY, roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    // JWT를 복호화하여 Spring Security의 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        UserDetails principal = User.withUsername(claims.getSubject())
                .password("") // JWT 기반 인증에선 비밀번호 불필요
                .authorities(authorities)
                .build();
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 주어진 토큰의 유효성(서명, 만료일 등) 검증
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // 토큰에서 사용자 식별자(subject)만 추출
    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰을 파싱하여 서명을 검증하고 Jws<Claims> 객체 반환(만료 시간도 검증)
    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    // 토큰의 본문(payload)에 해당하는 Claims를 반환
    private Claims parseClaims(String token) {
        return parse(token).getBody();
    }

    // Claims에서 권한 정보를 추출하여 GrantedAuthority 리스트로 변환
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object rawAuthorities = claims.get(AUTHORITIES_KEY);
        if (rawAuthorities instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(StringUtils::hasText)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    // 설정 파일의 시크릿 문자열을 디코딩하여 바이트 배열로 변환
    private byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw ex;
            }
            return bytes;
        }
    }
}
