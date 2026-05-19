package com.hwayoung.hwayoungserver.user.dto;

import com.hwayoung.hwayoungserver.user.User;

import java.util.UUID;

public record SignupResponse(
        UUID userId,
        String email,
        String name
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getName());
    }
}
