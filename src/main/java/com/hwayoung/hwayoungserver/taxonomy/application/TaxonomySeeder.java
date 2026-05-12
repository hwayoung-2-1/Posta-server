package com.hwayoung.hwayoungserver.taxonomy.application;

import com.hwayoung.hwayoungserver.taxonomy.domain.model.RoleEntity;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;
import com.hwayoung.hwayoungserver.taxonomy.persistence.RoleRepository;
import com.hwayoung.hwayoungserver.taxonomy.persistence.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaxonomySeeder implements ApplicationRunner {
    private static final List<String> DEFAULT_ROLES = List.of(
            "Front-End",
            "Back-End",
            "Full-Stack",
            "UX/UI Design",
            "Product Manager"
    );

    private static final List<String> DEFAULT_SKILLS = List.of(
            "React",
            "Spring",
            "Java",
            "Figma",
            "Illustrator",
            "Photoshop",
            "PostgreSQL"
    );

    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        DEFAULT_ROLES.stream()
                .filter(name -> roleRepository.findByName(name).isEmpty())
                .map(RoleEntity::new)
                .forEach(roleRepository::save);
        DEFAULT_SKILLS.stream()
                .filter(name -> skillRepository.findByName(name).isEmpty())
                .map(SkillEntity::new)
                .forEach(skillRepository::save);
    }
}
