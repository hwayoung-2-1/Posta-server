package com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response;

import com.hwayoung.hwayoungserver.taxonomy.domain.model.RoleEntity;

import java.util.UUID;

public record RoleItemResponse(
        UUID roleId,
        String name
) {
    public static RoleItemResponse from(RoleEntity role) {
        return new RoleItemResponse(role.getId(), role.getName());
    }
}
