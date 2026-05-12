package com.hwayoung.hwayoungserver.user;

import com.hwayoung.hwayoungserver.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {
            throw ApiException.unauthorized("인증이 필요합니다.");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "유저를 찾을 수 없습니다."));
    }

    public Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(authentication.getName());
    }
}
