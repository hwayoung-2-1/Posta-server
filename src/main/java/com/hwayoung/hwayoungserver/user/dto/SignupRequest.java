package com.hwayoung.hwayoungserver.user.dto;

public record SignupRequest(
        String email,
        String password,
        String name
) {
}
