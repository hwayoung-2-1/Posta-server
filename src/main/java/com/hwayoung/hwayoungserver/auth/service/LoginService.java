package com.hwayoung.hwayoungserver.auth.service;

import com.hwayoung.hwayoungserver.auth.dto.LoginRequest;
import com.hwayoung.hwayoungserver.auth.dto.LoginResponse;
import com.hwayoung.hwayoungserver.auth.exception.InvalidPasswordException;
import com.hwayoung.hwayoungserver.auth.exception.UserNotFoundException;
import com.hwayoung.hwayoungserver.auth.util.JwtUtil;
import com.hwayoung.hwayoungserver.user.entity.UserInfo;
import com.hwayoung.hwayoungserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        UserInfo userInfo = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (!passwordEncoder.matches(password, userInfo.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다");
        }

        /**
         * 로그인시 기존 유효 토큰 삭제
         */
        if (accessTokenService.isAlreadyLogin(userInfo.getId())) {
            accessTokenService.deleteValidTokenByUserId(userInfo.getId());
            System.out.println("기존 유효 토큰 삭제됨");
        }

        String accessToken = jwtUtil.generateToken(email);
        accessTokenService.saveToken(accessToken, userInfo.getId());

        return LoginResponse.of(accessToken);
    }

    public void logout(String token) {
        accessTokenService.logout(token);
    }
}
