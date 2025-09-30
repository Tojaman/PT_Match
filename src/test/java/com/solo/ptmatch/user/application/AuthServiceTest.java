package com.solo.ptmatch.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.user.domain.Role;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import com.solo.ptmatch.user.presentation.request.RegisterRequest;
import com.solo.ptmatch.user.presentation.response.RegisterResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class) // Mockito 기능을 JUnit 5에서 사용하기 위한 설정
class AuthServiceTest {

    @InjectMocks // 테스트 대상 클래스. @Mock으로 생성된 객체들이 자동으로 주입됨.
    private AuthService authService;

    @Mock // 테스트 대상 클래스가 의존하는 객체를 Mock(가짜)으로 생성
    private UserRepository userRepository;

    @Mock // 테스트 대상 클래스가 의존하는 객체를 Mock(가짜)으로 생성
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입: 성공 - 이메일이 중복되지 않으면 회원 정보를 저장하고 반환한다")
    void registerSucceedsWhenEmailIsNotDuplicated() {
        // given (주어진 상황)
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", "김헬스", Role.USER);
        String encodedPassword = "encoded_password";

        // userRepository.existsByEmail()이 false를 반환하도록 설정
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        // passwordEncoder.encode()가 "encoded_password"를 반환하도록 설정
        given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
        // userRepository.save()가 호출되면 저장된 User 객체를 반환하도록 설정
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when (무엇을 할 때)
        RegisterResponse response = authService.register(request);

        // then (결과는 이렇다)
        // 1. 결과 값 검증
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.role()).isEqualTo(request.role());

        // 2. userRepository.save()가 올바른 인자와 함께 호출되었는지 검증
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture()); // save 메서드가 호출되었는지 확인하고 인자를 캡처
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(request.email());
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword); // 암호화된 비밀번호가 저장되었는지 확인
        assertThat(savedUser.getName()).isEqualTo(request.name());
        assertThat(savedUser.getRole()).isEqualTo(request.role());

        // 3. 다른 메서드들이 정확히 호출되었는지 검증
        then(userRepository).should().existsByEmail(request.email());
        then(passwordEncoder).should().encode(request.password());
    }

    @Test
    @DisplayName("회원가입: 실패 - 이메일이 중복되면 예외를 발생시킨다")
    void registerFailsWhenEmailIsDuplicated() {
        // given
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", "김헬스", Role.USER);

        // userRepository.existsByEmail()이 true를 반환하도록 설정
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        // authService.register(request)를 실행했을 때 GlobalException이 발생하는지 검증
        GlobalException exception = assertThrows(GlobalException.class, () -> authService.register(request));

        // 발생한 예외의 ErrorCode가 USER_EMAIL_DUPLICATED인지 확인
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EMAIL_DUPLICATED);

        // 이메일 중복 시, 비밀번호 암호화나 저장은 절대 호출되면 안 됨
        then(passwordEncoder).should(never()).encode(any());
        then(userRepository).should(never()).save(any());
    }
}