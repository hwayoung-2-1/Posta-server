package com.hwayoung.hwayoungserver.auth.service;

import com.hwayoung.hwayoungserver.auth.dto.LoginRequest;
import com.hwayoung.hwayoungserver.auth.dto.LoginResponse;
import com.hwayoung.hwayoungserver.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // 이메일 확인
        // 비밀번호 검증

        String accessToken = jwtUtil.generateToken(email);
        // jwt 토큰 처리

        return LoginResponse.of(accessToken);
    }
}
