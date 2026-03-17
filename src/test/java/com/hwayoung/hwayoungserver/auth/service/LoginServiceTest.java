package com.hwayoung.hwayoungserver.auth.service;

import com.hwayoung.hwayoungserver.auth.dto.LoginRequest;
import com.hwayoung.hwayoungserver.auth.dto.LoginResponse;
import com.hwayoung.hwayoungserver.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginServiceTest {
    private JwtUtil jwtUtil;
    private LoginService loginService;

    private LoginRequest loginRequest;
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        // 실제 JwtUtil 인스턴스 생성
        String jwtSecret = "we-are-bssm-project-internship-hwayoung-team1";
        long jwtExpiration = 43200000L; // 12시간
        jwtUtil = new JwtUtil(jwtSecret, jwtExpiration);

        // 실제 LoginService 인스턴스 생성
        loginService = new LoginService(jwtUtil);

        testEmail = "test@example.com";
        testPassword = "password123";

        loginRequest = new LoginRequest();
        try {
            var emailField = LoginRequest.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(loginRequest, testEmail);

            var passwordField = LoginRequest.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(loginRequest, testPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("로그인 성공 - 실제 JWT 토큰이 발급된다")
    void login_Success_ReturnsActualJwtToken() {
        // Given: Mock 데이터로 이메일과 비밀번호가 준비되어 있음
        // DB 관련 Mock 설정 (향후 구현 시 추가)
        // User mockUser = User.builder()
        //     .email(testEmail)
        //     .password("$2a$10$encodedPassword") // BCrypt로 인코딩된 비밀번호
        //     .build();
        // when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));
        // when(passwordEncoder.matches(testPassword, mockUser.getPassword())).thenReturn(true);

        // When: 실제 로그인 메서드 호출하여 JWT 토큰 발급
        LoginResponse response = loginService.login(loginRequest);

        // Then: 응답 검증
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        // JWT 토큰 형식 검증 (header.payload.signature)
        String[] jwtParts = response.getAccessToken().split("\\.");
        assertThat(jwtParts).hasSize(3);

        System.out.println("발급된 JWT 토큰: " + response.getAccessToken());
    }

    @Test
    @DisplayName("발급된 JWT 토큰이 유효하다")
    void login_GeneratedTokenIsValid() {
        // Given & When: 로그인하여 JWT 토큰 발급
        LoginResponse response = loginService.login(loginRequest);
        String accessToken = response.getAccessToken();

        // Then: 토큰이 유효한지 검증
        boolean isValid = jwtUtil.validateToken(accessToken);
        assertThat(isValid).isTrue();

        System.out.println("발급된 토큰: " + accessToken);
        System.out.println("토큰 유효성: " + isValid);
    }

    @Test
    @DisplayName("동일한 사용자로 여러 번 로그인해도 모두 유효한 토큰이 발급된다")
    void login_MultipleLoginsGenerateValidTokens() throws InterruptedException {
        // Given & When: 같은 이메일로 두 번 로그인 (시간차를 두기 위해 1초 대기)
        LoginResponse response1 = loginService.login(loginRequest);
        Thread.sleep(1000); // 1초 대기하여 다른 토큰 생성
        LoginResponse response2 = loginService.login(loginRequest);

        // Then: 두 토큰 모두 유효한지 확인
        assertThat(jwtUtil.validateToken(response1.getAccessToken())).isTrue();
        assertThat(jwtUtil.validateToken(response2.getAccessToken())).isTrue();

        // 시간차가 있으면 다른 토큰이 발급됨
        assertThat(response1.getAccessToken()).isNotEqualTo(response2.getAccessToken());

        System.out.println("첫 번째 토큰: " + response1.getAccessToken());
        System.out.println("두 번째 토큰: " + response2.getAccessToken());
    }

    // 향후 DB 구현 시 추가할 테스트 케이스들

    // @Test
    // @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    // void login_Fail_UserNotFound() {
    //     // Given
    //     when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
    //
    //     // When & Then
    //     assertThatThrownBy(() -> loginService.login(loginRequest))
    //         .isInstanceOf(IllegalArgumentException.class)
    //         .hasMessage("존재하지 않는 사용자입니다.");
    // }

    // @Test
    // @DisplayName("로그인 실패 - 비밀번호 불일치")
    // void login_Fail_WrongPassword() {
    //     // Given
    //     User mockUser = User.builder()
    //         .email(testEmail)
    //         .password("$2a$10$encodedPassword")
    //         .build();
    //     when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));
    //     when(passwordEncoder.matches(testPassword, mockUser.getPassword())).thenReturn(false);
    //
    //     // When & Then
    //     assertThatThrownBy(() -> loginService.login(loginRequest))
    //         .isInstanceOf(IllegalArgumentException.class)
    //         .hasMessage("비밀번호가 일치하지 않습니다.");
    // }
}
