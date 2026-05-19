package com.hwayoung.hwayoungserver.user;

import com.hwayoung.hwayoungserver.portfolio.persistence.PortfolioRepository;
import com.hwayoung.hwayoungserver.portfolio.domain.type.PortfolioStatus;
import com.hwayoung.hwayoungserver.user.dto.UpdateUserRequest;
import com.hwayoung.hwayoungserver.user.dto.UpdateUserResponse;
import com.hwayoung.hwayoungserver.user.dto.UserMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final CurrentUserService currentUserService;
    private final PortfolioRepository portfolioRepository;

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserMeResponse> me() {
        User user = currentUserService.getCurrentUser();
        long portfolioCount = portfolioRepository.countByOwnerAndStatusNot(user, PortfolioStatus.DELETED);
        return ResponseEntity.ok(UserMeResponse.of(user, portfolioCount));
    }

    @PatchMapping("/me")
    @Transactional
    public ResponseEntity<UpdateUserResponse> updateMe(@RequestBody UpdateUserRequest request) {
        User user = currentUserService.getCurrentUser();
        user.updateProfile(request.name(), request.profileImageUrl());
        return ResponseEntity.ok(UpdateUserResponse.from(user));
    }
}
