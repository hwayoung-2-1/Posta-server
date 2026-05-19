package com.hwayoung.hwayoungserver.auth.service;

import com.hwayoung.hwayoungserver.common.ApiException;
import com.hwayoung.hwayoungserver.user.User;
import com.hwayoung.hwayoungserver.user.UserRepository;
import com.hwayoung.hwayoungserver.user.dto.SignupRequest;
import com.hwayoung.hwayoungserver.user.dto.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validate(request);
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.badRequest("이미 가입된 이메일입니다.");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );
        return SignupResponse.from(userRepository.save(user));
    }

    private void validate(SignupRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password()) || isBlank(request.name())) {
            throw ApiException.badRequest("이메일, 비밀번호, 이름은 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
