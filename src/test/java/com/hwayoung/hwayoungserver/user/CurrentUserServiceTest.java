package com.hwayoung.hwayoungserver.user;

import com.hwayoung.hwayoungserver.common.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {
    @Mock
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JWT 인증 주체로 현재 사용자를 조회한다")
    void getCurrentUserFindsUserFromAuthenticationPrincipal() {
        CurrentUserService currentUserService = new CurrentUserService(userRepository);
        User user = user("member@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member@example.com", null, List.of())
        );
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));

        User currentUser = currentUserService.getCurrentUser();

        assertThat(currentUser.getId()).isEqualTo(user.getId());
        verify(userRepository).findByEmail("member@example.com");
    }

    @Test
    @DisplayName("JWT 인증 정보가 없으면 현재 사용자 조회에 실패한다")
    void getCurrentUserFailsWithoutAuthentication() {
        CurrentUserService currentUserService = new CurrentUserService(userRepository);

        assertThatThrownBy(currentUserService::getCurrentUser)
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("UNAUTHORIZED");
    }

    private User user(String email) {
        User user = new User(email, "password", "name");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
