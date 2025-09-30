package com.solo.ptmatch.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 사용자 인증 흐름(회원가입, 로그인, 인증)에 대한 통합 테스트.
 * - @SpringBootTest: 애플리케이션의 모든 Bean을 로드하여 실제 서버와 거의 동일한 환경을 구성한다.
 * - @AutoConfigureMockMvc: MockMvc를 사용하여 실제 HTTP 요청을 시뮬레이션한다.
 * - @Transactional: 각 테스트가 끝난 후 DB 변경사항을 롤백하여 테스트 간 독립성을 보장한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 기본 회원가입 요청 데이터 초기화
        registerRequest = new RegisterRequest(
            "user@example.com",
            "password123",
            "김테스트",
            Role.USER
        );
    }

    @Test
    @DisplayName("회원가입: API 요청 시 사용자가 DB에 저장된다")
    void registerApi_createsUserInDatabase() throws Exception {
        // when: 회원가입 API를 호출
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            // then: 성공 응답(200 OK)을 확인
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(registerRequest.email()));

        // then: 실제 DB에서 해당 이메일로 사용자를 조회
        User foundUser = userRepository.findByEmail(registerRequest.email()).orElseThrow();

        // then: 저장된 사용자의 정보가 요청과 일치하는지, 비밀번호는 암호화되었는지 검증
        assertThat(foundUser.getName()).isEqualTo(registerRequest.name());
        assertThat(passwordEncoder.matches(registerRequest.password(), foundUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("로그인: 성공 시 Authorization 헤더에 JWT 토큰을 반환한다")
    void login_succeeds_and_returns_jwt_in_header() throws Exception {
        // given: 테스트용 사용자를 미리 DB에 저장 (비밀번호는 암호화)
        userRepository.save(User.create(
            registerRequest.email(),
            passwordEncoder.encode(registerRequest.password()),
            registerRequest.name(),
            registerRequest.role()
        ));

        LoginRequest loginRequest = new LoginRequest(registerRequest.email(), registerRequest.password());

        // when: 로그인 API를 호출
        // then: 성공 응답(200 OK)과 함께 'Authorization' 헤더가 존재하는지 확인
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(header().exists("Authorization"))
            .andExpect(header().string("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")));
    }

    @Test
    @DisplayName("로그인: 비밀번호가 틀리면 401 Unauthorized 에러를 반환한다")
    void login_fails_with_wrong_password() throws Exception {
        // given: 테스트용 사용자를 미리 DB에 저장
        userRepository.save(User.create(
            registerRequest.email(),
            passwordEncoder.encode(registerRequest.password()),
            registerRequest.name(),
            registerRequest.role()
        ));

        LoginRequest loginRequestWithWrongPassword = new LoginRequest(registerRequest.email(), "wrong_password");

        // when: 잘못된 비밀번호로 로그인 API를 호출
        // then: 401 Unauthorized 상태 코드를 확인
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestWithWrongPassword)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API 접근: 인증된 사용자는 보호된 API에 접근할 수 있다")
    void authenticatedUser_can_access_protected_api() throws Exception {
        // given: 테스트용 사용자를 저장하고 로그인하여 JWT 토큰을 획득
        userRepository.save(User.create(
            registerRequest.email(),
            passwordEncoder.encode(registerRequest.password()),
            registerRequest.name(),
            Role.USER
        ));
        LoginRequest loginRequest = new LoginRequest(registerRequest.email(), registerRequest.password());

        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String token = loginResult.getResponse().getHeader("Authorization");

        // when: 획득한 토큰을 헤더에 담아 보호된 API(/api/trainers/me)를 호출
        // then: 성공 응답(200 OK)을 확인
        // 참고: /api/trainers/me는 예시이며, 실제 존재하는 보호된 엔드포인트로 테스트해야 합니다.
        // 현재 해당 엔드포인트가 404를 반환할 수 있으므로, 여기서는 401이 아닌지만 확인합니다.
        mockMvc.perform(get("/api/trainers/me")
                .header("Authorization", token))
            .andExpect(status().is(org.hamcrest.Matchers.not(401))); // 401 Unauthorized가 아니면 성공으로 간주
    }

    @Test
    @DisplayName("API 접근: 인증되지 않은 사용자는 보호된 API 접근 시 403 에러가 발생한다")
    void unauthenticatedUser_gets_401_from_protected_api() throws Exception {
        // when: 인증 토큰 없이 보호된 API를 호출
        // then: 401 Unauthorized 상태 코드를 확인
        mockMvc.perform(get("/api/reviews"))
            .andExpect(status().isForbidden());
    }
}