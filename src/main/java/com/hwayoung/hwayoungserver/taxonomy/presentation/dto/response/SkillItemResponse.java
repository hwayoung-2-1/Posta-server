package com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response;

import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;

import java.util.UUID;

public record SkillItemResponse(
        UUID skillId,
        String name
) {
    public static SkillItemResponse from(SkillEntity skill) {
        return new SkillItemResponse(skill.getId(), skill.getName());
    }
}
