package com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response;

import java.util.List;

public record SkillsResponse(
        List<SkillItemResponse> skills
) {
}
