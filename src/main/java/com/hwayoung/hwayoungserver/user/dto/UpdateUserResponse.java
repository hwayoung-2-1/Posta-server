package com.hwayoung.hwayoungserver.user.dto;

import com.hwayoung.hwayoungserver.user.User;

import java.util.UUID;

public record UpdateUserResponse(
        UUID userId,
        String name,
        String profileImageUrl
) {
    public static UpdateUserResponse from(User user) {
        return new UpdateUserResponse(user.getId(), user.getName(), user.getProfileImageUrl());
    }
}
