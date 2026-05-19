package com.hwayoung.hwayoungserver.taxonomy.presentation;

import com.hwayoung.hwayoungserver.taxonomy.domain.model.RoleEntity;
import com.hwayoung.hwayoungserver.taxonomy.domain.model.SkillEntity;
import com.hwayoung.hwayoungserver.taxonomy.persistence.RoleRepository;
import com.hwayoung.hwayoungserver.taxonomy.persistence.SkillRepository;
import com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response.RoleItemResponse;
import com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response.RolesResponse;
import com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response.SkillItemResponse;
import com.hwayoung.hwayoungserver.taxonomy.presentation.dto.response.SkillsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaxonomyController {
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;

    @GetMapping("/roles")
    @Transactional(readOnly = true)
    public ResponseEntity<RolesResponse> roles() {
        return ResponseEntity.ok(new RolesResponse(
                roleRepository.findAll().stream()
                        .sorted(Comparator.comparing(RoleEntity::getName))
                        .map(RoleItemResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/skills")
    @Transactional(readOnly = true)
    public ResponseEntity<SkillsResponse> skills() {
        return ResponseEntity.ok(new SkillsResponse(
                skillRepository.findAll().stream()
                        .sorted(Comparator.comparing(SkillEntity::getName))
                        .map(SkillItemResponse::from)
                        .toList()
        ));
    }
}
