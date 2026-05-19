package com.hwayoung.hwayoungserver.user.dto;

public record UpdateUserRequest(
        String name,
        String profileImageUrl
) {
}
