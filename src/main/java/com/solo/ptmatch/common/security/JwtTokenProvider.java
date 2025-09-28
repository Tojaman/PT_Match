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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "roles";
    private static final String DEFAULT_SECRET = "ZmFrZS1zZWNyZXQta2V5LWZvci1kZXYtcGFzcy0xMjM0NTY=";

    private final SecretKey secretKey;
    private final long accessTokenValidityMillis;

    public JwtTokenProvider(
        @Value("${security.jwt.secret:}") String secret,
        @Value("${security.jwt.access-token-validity-ms:3600000}") long accessTokenValidityMillis
    ) {
        String resolvedSecret = StringUtils.hasText(secret) ? secret : DEFAULT_SECRET;
        byte[] keyBytes = decodeSecret(resolvedSecret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes)");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityMillis = accessTokenValidityMillis;
    }

    public String generateAccessToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenValidityMillis);
        List<String> roles = authorities == null ? List.of() : authorities.stream()
            .filter(Objects::nonNull)
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return Jwts.builder()
            .subject(subject)
            .claim(AUTHORITIES_KEY, roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        User principal = User.withUsername(claims.getSubject())
            .password("")
            .authorities(authorities)
            .accountLocked(false)
            .accountExpired(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token);
    }

    private Claims parseClaims(String token) {
        return parse(token).getBody();
    }

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
