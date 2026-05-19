package com.hwayoung.hwayoungserver.auth.service;

import com.hwayoung.hwayoungserver.auth.dto.LoginRequest;
import com.hwayoung.hwayoungserver.auth.dto.LoginResponse;
import com.hwayoung.hwayoungserver.auth.util.JwtUtil;
import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.user.User;
import com.hwayoung.hwayoungserver.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.userRepository = null;
        this.passwordEncoder = null;
    }

    @Autowired
    public LoginService(JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw ApiException.badRequest("이메일과 비밀번호는 필수입니다.");
        }

        String email = request.getEmail();
        String password = request.getPassword();

        if (userRepository != null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다."));
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다.");
            }
        }

        String accessToken = jwtUtil.generateToken(email);

        return LoginResponse.of(accessToken);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
