package com.hwayoung.hwayoungserver.user.dto;

import com.hwayoung.hwayoungserver.user.User;

import java.util.UUID;

public record UserMeResponse(
        UUID userId,
        String email,
        String name,
        String profileImageUrl,
        long portfolioCount
) {
    public static UserMeResponse of(User user, long portfolioCount) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                portfolioCount
        );
    }
}
