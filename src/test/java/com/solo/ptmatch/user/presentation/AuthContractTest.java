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
import com.solo.ptmatch.user.application.AuthService;
import com.solo.ptmatch.user.application.dto.LoginCommand;
import com.solo.ptmatch.user.application.dto.LoginResult;
import com.solo.ptmatch.user.application.dto.RegisterCommand;
import com.solo.ptmatch.user.application.dto.RegisterResult;
import com.solo.ptmatch.user.domain.UserRole;
import com.solo.ptmatch.user.presentation.request.LoginRequest;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("회원가입 API")
    class Register {

        @Test
        @DisplayName("정상 요청 시 생성된 회원 정보를 반환한다")
        void registerReturnsCreatedUserSummary() throws Exception {
            // given
            RegisterRequest requestPayload = new RegisterRequest(
                "user@example.com",
                "password123",
                "김헬스",
                UserRole.USER
            );

            RegisterResult serviceResult = new RegisterResult(1L, "user@example.com", "김헬스", UserRole.USER);

            given(authService.register(any(RegisterCommand.class))).willReturn(serviceResult);

            // when
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestPayload)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("김헬스"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.pageResponse").doesNotExist());

            ArgumentCaptor<RegisterCommand> registerCaptor = ArgumentCaptor.forClass(RegisterCommand.class);
            then(authService).should().register(registerCaptor.capture());

            RegisterCommand capturedCommand = registerCaptor.getValue();
            assertThat(capturedCommand.email()).isEqualTo("user@example.com");
            assertThat(capturedCommand.password()).isEqualTo("password123");
            assertThat(capturedCommand.name()).isEqualTo("김헬스");
            assertThat(capturedCommand.role()).isEqualTo(UserRole.USER);
        }
    }

    @Nested
    @DisplayName("로그인 API")
    class Login {

        @Test
        @DisplayName("정상 요청 시 액세스 토큰을 반환한다")
        void loginReturnsAccessToken() throws Exception {
            // given
            LoginRequest requestPayload = new LoginRequest("user@example.com", "password123");
            LoginResult serviceResult = new LoginResult("jwt.token.string");

            given(authService.login(any(LoginCommand.class))).willReturn(serviceResult);

            // when
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestPayload)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt.token.string"))
                .andExpect(jsonPath("$.pageResponse").doesNotExist());

            ArgumentCaptor<LoginCommand> loginCaptor = ArgumentCaptor.forClass(LoginCommand.class);
            then(authService).should().login(loginCaptor.capture());

            LoginCommand capturedCommand = loginCaptor.getValue();
            assertThat(capturedCommand.email()).isEqualTo("user@example.com");
            assertThat(capturedCommand.password()).isEqualTo("password123");
        }
    }
}
