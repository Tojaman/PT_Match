package com.solo.ptmatch.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                return message;
            }

            String rawToken = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.hasText(rawToken)) {
                rawToken = accessor.getFirstNativeHeader("accessToken");
            }
            String token = resolveToken(rawToken);
            if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
                throw new IllegalArgumentException("유효한 JWT 토큰이 필요합니다.");
            }
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            accessor.setUser(authentication);
            return message;
        }

        if (accessor.getUser() == null
            && (StompCommand.SEND.equals(accessor.getCommand())
            || StompCommand.SUBSCRIBE.equals(accessor.getCommand())
            || SimpMessageType.MESSAGE.equals(accessor.getMessageType()))) {
            throw new IllegalArgumentException("인증된 STOMP 사용자만 접근할 수 있습니다.");
        }

        return message;
    }

    private String resolveToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return null;
        }
        if (rawToken.startsWith("Bearer ")) {
            return rawToken.substring(7);
        }
        return rawToken;
    }
}
