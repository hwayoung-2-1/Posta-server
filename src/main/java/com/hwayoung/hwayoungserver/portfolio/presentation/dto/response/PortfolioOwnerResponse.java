package com.hwayoung.hwayoungserver.portfolio.presentation.dto.response;

import com.hwayoung.hwayoungserver.user.User;

import java.util.UUID;

public record PortfolioOwnerResponse(
        UUID userId,
        String name,
        String profileImageUrl
) {
    public static PortfolioOwnerResponse from(User user) {
        return new PortfolioOwnerResponse(user.getId(), user.getName(), user.getProfileImageUrl());
    }
}
