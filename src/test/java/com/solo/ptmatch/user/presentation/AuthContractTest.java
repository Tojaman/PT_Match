package com.solo.ptmatch.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.common.security.JwtTokenProvider;
import com.solo.ptmatch.user.application.AuthService;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 컨트롤러 슬라이스 테스트 (Controller Slice Test)
 * - @WebMvcTest 어노테이션을 통해 웹 계층(Controller, Filter, Converter 등)에 관련된 Bean만 로드하여 테스트 환경을 구성한다.
 * - 컨트롤러의 주요 역할(API 엔드포인트 매핑, 요청/응답 데이터 변환 등)을 검증하는 데 목적이 있다.
 */
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 체인을 비활성화하여 인증/인가 없이 컨트롤러를 테스트
@WebMvcTest(AuthController.class) // 테스트할 컨트롤러를 지정. 여기서는 AuthController만 메모리에 로드
class AuthContractTest {

    // MockMvc: 실제 서버를 띄우지 않고, 가상의 HTTP 요청을 보내고 응답을 검증할 수 있게 해주는 객체
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper: Java 객체와 JSON 문자열 간의 변환을 담당
    @Autowired
    private ObjectMapper objectMapper;

    // @MockitoBean: Spring 테스트 컨텍스트에 실제 Bean 대신 Mockito로 생성한 Mock(가짜) 객체를 등록
    // 여기서는 Controller가 의존하는 AuthService의 실제 로직을 실행하지 않고, 미리 정의된 행동을 하도록 만들기 위해 사용
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider; // SecurityConfig가 의존하므로 Mock 객체로 등록 필요

    @Test
    @DisplayName("회원가입 API: 정상 요청 시 생성된 회원 정보를 반환한다")
    void registerReturnsCreatedUserSummary() throws Exception {
        // given - 테스트를 위한 사전 준비 단계
        // 1. 컨트롤러에 전달할 요청 데이터(payload) 생성
        RegisterRequest requestPayload = new RegisterRequest(
            "user@example.com",
            "password123",
            "김헬스",
            Role.USER
        );

        // 2. Mock AuthService가 반환할 결과(가짜 데이터)를 미리 정의
        RegisterResponse serviceResult = new RegisterResponse(1L, "user@example.com", "김헬스", Role.USER);
        given(authService.register(any(RegisterRequest.class))).willReturn(serviceResult);

        // when - 실제 테스트 동작을 수행하는 단계
        // MockMvc를 통해 /api/auth/register 엔드포인트로 POST 요청을 보냄
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
            // then - 수행 결과가 예상과 일치하는지 검증하는 단계
            // 1. HTTP 응답 검증
            .andExpect(status().isOk()) // 응답 코드가 200 OK 인지 확인
            .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 응답의 Content-Type이 JSON인지 확인
            // 2. JSON 응답 본문(body)의 각 필드 값 검증
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.email").value("user@example.com"))
            .andExpect(jsonPath("$.data.name").value("김헬스"))
            .andExpect(jsonPath("$.data.role").value("USER"))
            .andExpect(jsonPath("$.pageResponse").doesNotExist());

        // then - 추가 검증: 컨트롤러가 서비스를 올바르게 호출했는지 확인
        // 1. authService.register() 메서드로 전달된 인자를 캡처
        ArgumentCaptor<RegisterRequest> registerCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
        then(authService).should().register(registerCaptor.capture());

        // 2. 캡처된 인자의 값이 예상과 일치하는지 확인
        RegisterRequest capturedCommand = registerCaptor.getValue();
        assertThat(capturedCommand.email()).isEqualTo("user@example.com");
        assertThat(capturedCommand.password()).isEqualTo("password123");
        assertThat(capturedCommand.name()).isEqualTo("김헬스");
        assertThat(capturedCommand.role()).isEqualTo(Role.USER);
    }
}
