package com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response;

import java.util.List;

public record RolesResponse(
        List<RoleItemResponse> roles
) {
}
