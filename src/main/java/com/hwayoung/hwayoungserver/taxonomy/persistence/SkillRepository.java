package com.hwayoung.hwayoungserver.taxonomy.persistence;

import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<SkillEntity, UUID> {
    Optional<SkillEntity> findByName(String name);
}
