package com.solo.ptmatch.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security의 핵심 필터 체인 설정
     * HTTP 보안 규칙, 세션 관리, CORS, CSRF, 필터 추가 등 애플리케이션의 전반적인 보안 설정 구성
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFilter loginFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF(Cross-Site Request Forgery) 보호 비활성화 (Stateless 서버)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS(Cross-Origin Resource Sharing) 설정
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 정책을 STATELESS로 설정 (세션 사용 안함)
            .formLogin(AbstractHttpConfigurer::disable) // 기본 form 로그인 비활성화
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
            .authorizeHttpRequests(authorize -> authorize // HTTP 요청에 대한 인가 규칙 설정
                .requestMatchers("/api/auth/register", "/login", "/api/v1/auth/register", "/api/v1/auth/login").permitAll() // 회원가입/로그인 API는 인증 없이 허용
                .requestMatchers(HttpMethod.GET, "/api/trainers", "/api/trainers/*").permitAll() // 트레이너 정보 조회(GET)는 인증 없이 허용
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").permitAll() // 상품 정보 조회(GET)는 인증 없이 허용
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll() // API 문서 및 헬스 체크는 인증 없이 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS Preflight 요청(OPTIONS)은 인증 없이 허용
                .requestMatchers(HttpMethod.POST, "/api/trainers/me").hasRole("TRAINER") // 트레이너 프로필 등록/수정은 TRAINER 역할만 가능
                .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
            )
            .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class) // 로그인 처리를 위한 커스텀 필터(LoginFilter)를 기본 필터 위치에 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 토큰 검증을 위한 커스텀 필터(JwtAuthenticationFilter)를 로그인 필터 전에 추가

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
     * CORS(Cross-Origin Resource Sharing) 설정을 위한 CorsConfigurationSource를 Bean으로 등록
     * 다른 도메인에서의 요청을 허용하는 정책 정의
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // CORS 허용할 도메인(로컬)
        configuration.setAllowedMethods(List.of("*")); // 모든 HTTP 메소드 허용
        configuration.setAllowedHeaders(List.of("*")); // 모든 HTTP 헤더 허용
        configuration.setAllowCredentials(false); // 인증 정보 포함 비허용(헤더에 토큰 담기)
        configuration.setExposedHeaders(List.of("Authorization")); // Authorization 헤더 노출
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 커스텀 로그인 필터인 JwtLoginFilter를 Bean으로 등록 설정
     * 인증 관리자, JWT 토큰 제공자, 성공/실패 핸들러를 주입하여 필터를 생성
     */
    @Bean
    public LoginFilter jwtLoginFilter(
        AuthenticationManager authenticationManager,
        JwtTokenProvider jwtTokenProvider,
        ObjectMapper objectMapper
    ) {
        return new LoginFilter(authenticationManager, jwtTokenProvider, objectMapper);
    }
}
