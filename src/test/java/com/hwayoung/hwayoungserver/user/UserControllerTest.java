package com.hwayoung.hwayoungserver.user;

import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioRepository;
import com.hwayoung.hwayoungserver.user.dto.UserMeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private PortfolioRepository portfolioRepository;

    @Test
    @DisplayName("내 정보 조회는 요청 데이터 없이 JWT 현재 사용자 정보를 반환한다")
    void meReturnsCurrentUserInfoWithoutRequestData() {
        UserController userController = new UserController(currentUserService, portfolioRepository);
        User user = user("member@example.com", "민지");
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(portfolioRepository.countByOwnerAndStatusNot(user, PortfolioStatus.DELETED)).thenReturn(3L);

        ResponseEntity<UserMeResponse> response = userController.me();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(user.getId());
        assertThat(response.getBody().email()).isEqualTo("member@example.com");
        assertThat(response.getBody().name()).isEqualTo("민지");
        assertThat(response.getBody().portfolioCount()).isEqualTo(3L);
        verify(currentUserService).getCurrentUser();
    }

    private User user(String email, String name) {
        User user = new User(email, "password", name);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
