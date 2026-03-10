package com.solo.ptmatch.common.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security의 핵심 필터 체인 설정
     * HTTP 보안 규칙, 세션 관리, CORS, CSRF, 필터 추가 등 애플리케이션의 전반적인 보안 설정 구성
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (Stateless 서버)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 정책
                                                                                                              // STATELESS
                .formLogin(AbstractHttpConfigurer::disable) // 기본 form 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .authorizeHttpRequests(authorize -> authorize // HTTP 요청에 대한 인가 규칙 설정
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh",
                                "/api/auth/logout")
                        .permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll() // 외부 웹훅 (토스페이먼츠 등)
                        .requestMatchers(HttpMethod.POST, "/api/users/me/verification").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/trainers/me").hasRole("TRAINER") // ID 와일드카드보다 먼저 선언하여 보호
                        .requestMatchers(HttpMethod.GET, "/api/map/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/trainers", "/api/trainers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health",
                                "/actuator/prometheus")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS Preflight 요청(OPTIONS)은 인증 없이 허용
                        .requestMatchers(HttpMethod.POST, "/api/trainers/me", "/api/products/",
                                "/api/products/{productId}")
                        .hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/{productId}").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/{productId}").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.POST, "/api/products/*/like").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/me/likes/products").hasRole("USER")
                        .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 커스텀 필터 로그인
                                                                                                       // 필터 앞에 추가

        return http.build();
    }

    /**
     * Spring Security의 인증을 총괄하는 AuthenticationManager를 Bean으로 등록
     * 사용자 인증을 처리하는 데 사용되며, JwtLoginFilter에서 필요로 함
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder를 Bean으로 등록
     * BCrypt 알고리즘을 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정을 위한 CorsConfigurationSource를 Bean으로 등록
     * 다른 도메인에서의 요청을 허용하는 정책 정의
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 운영 프론트엔드 도메인, 로컬 도메인 2개만 CORS 허용
        configuration.setAllowedOrigins(List.of("https://ptmatch.shop", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("*")); // 모든 HTTP 메소드 허용
        configuration.setAllowedHeaders(List.of("*")); // 모든 HTTP 헤더 허용
        configuration.setAllowCredentials(true); // 인증 정보 포함 비허용(헤더에 토큰 담기)
        configuration.setExposedHeaders(List.of("Authorization")); // Authorization 헤더 노출
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
